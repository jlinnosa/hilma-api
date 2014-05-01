package io.mikael.api.hilma.scraper;

import io.mikael.api.hilma.domain.Notice;
import io.mikael.api.hilma.domain.ScrapedLink;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SiteScraper {

    private static final Pattern LINK_ID_PATTERN = Pattern.compile("/([0-9]{4}-[0-9]{6})/");

    private static final Pattern CPV_PATTERN = Pattern.compile("\\(([0-9\\-]*)\\)");

    private static final Pattern TITLE_PATTERN = Pattern.compile("([IVXLCDM]*\\.[0-9]*(?:\\.[0-9]*)) (.*)");

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            "d.M.y H.m", "d.M.y H:m", "d.M.y", "d.M.y 'klo' H.m"
    ).stream().map(DateTimeFormatter::ofPattern).collect(Collectors.toList());

    /**
     * HILMA has several suprising alternative date formats.
     */
    private static Optional<LocalDateTime> parseLocalDateTime(final String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        return FORMATTERS.stream().map(f -> {
                    try {
                        return LocalDateTime.parse(input, f);
                    } catch (final DateTimeParseException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull).findFirst();
    }

    private static String findCode(final String text) {
        final Matcher m = CPV_PATTERN.matcher(text);
        m.find();
        return m.group(1);
    }

    private static Matcher find(final String text, final Pattern pattern) {
        final Matcher ret = pattern.matcher(text);
        if (ret.find()) {
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Parse a list of new scraped links out of a HTML InputStream.
     */
    public static List<ScrapedLink> parseNewLinks(final Document doc) throws IOException {
        final List<ScrapedLink> ret = new ArrayList<>();
        for (final Element e : doc.select("tr:has(td)")) {
            final Elements data = e.select("td");
            if (data.size() == 4) {
                final Element linkElement = data.get(3).children().first();
                final String link = linkElement.attr("href");
                final ScrapedLink.Builder bld = ScrapedLink.builder()
                        .id(find(link, LINK_ID_PATTERN).group(1))
                        .link(link).name(linkElement.text())
                        .type(data.get(3).select("span.meta").first().text());

                final Optional<LocalDateTime> published = parseLocalDateTime(data.get(1).text());
                if (published.isPresent()) {
                    bld.published(published.get());
                }
                final Optional<LocalDateTime> closes = parseLocalDateTime(data.get(2).text());
                if (closes.isPresent()) {
                    bld.closes(closes.get());
                }
                ret.add(bld.build());

            }
        }
        return ret;
    }

    /**
     * There is still some regrettable magic in here, and different types all mixed together.
     */
    public static Notice.Builder parseNotice(final Document doc) {
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        // link, html
        final Notice.Builder builder = Notice.builder().html(doc.outerHtml());

        final Element content = doc.select("div#mainContent").first();

        // id
        doc.select("form#login").stream()
                .map(e -> e.attr("action"))
                .map(action -> find(action, LINK_ID_PATTERN).group(1))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::id);

        // published
        content.select("div#datePublished").stream()
                .map(Element::text)
                .map(SiteScraper::parseLocalDateTime)
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst().ifPresent(builder::published);

        // closes
        final String closesSelector = String.join(",", Arrays.asList(
                "dt:contains(Tarjoukset tai osallistumishakemukset on toimitettava hankintayksikölle viimeistään) ~ dd",
                "dt:contains(IV.3.4 Tarjousten vastaanottamisen määräaika) ~ dd",
                "dt:contains(IV.3.4 Osallistumishakemusten vastaanottamisen määräaika) ~ dd"
        ));

        content.select(closesSelector).stream()
                .map(Element::text).map(SiteScraper::parseLocalDateTime)
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst().ifPresent(builder::closes);

        // note
        content.select("div.note").stream()
                .map(Element::text).findFirst().ifPresent(builder::note);

        // mainCpvCode
        final Elements cpvTable = content.select("table.CPV:has(tr > td > strong)");
        cpvTable.select("tr + tr > td").stream()
                .map(Element::text)
                .map(SiteScraper::findCode)
                .findFirst()
                .ifPresent(builder::mainCpvCode);

        content.select("dt:contains(Yhteinen hankintanimikkeistö \\(CPV\\): Pääkohde) ~ dd").stream()
                .map(Element::text)
                .map(SiteScraper::findCode)
                .findFirst()
                .ifPresent(builder::mainCpvCode);

        // organizationName

        final String organizationNameSelector = String.join(",", Arrays.asList(
                "dt:contains(I.1 Nimi, osoite ja yhteyspiste) + dd td:contains(Virallinen nimi) + td",
                "dt:contains(Hankintayksikön yhteystiedot) + dd td:contains(Hankintayksikkö) + td"
        ));

        final Optional<String> oon = content.select(organizationNameSelector).stream()
                .map(Element::text).findFirst();
        oon.ifPresent(builder::organizationName);

        // type, noticeName
        final List<String> s = content.select("h2").stream()
                .map(e -> e.childNodes())
                .flatMap(l -> l.stream())
                .map(Node::toString)
                .map(String::trim)
                .collect(Collectors.toList());

        builder.type(s.get(0).substring(0, s.get(0).length() - 1));

        final String rawName = s.get(2);
        if (oon.isPresent() && rawName.startsWith(oon.get())
                && rawName.length() > oon.get().length() + 3)
        {
            builder.noticeName(rawName.substring(oon.get().length() + 3));
        } else {
            builder.noticeName(rawName);
        }

        final String noticeDescriptionSelector = String.join(",", Arrays.asList(
                "dt:contains(II.1.4 Lyhyt kuvaus) ~ dd",
                "dt:contains(Hankinnan kuvaus) ~ dd",
                "dt:contains(II.1.5 Sopimuksen tai hankinnan \\(hankintojen\\) lyhyt kuvaus) ~ dd",
                "dt:contains(II.4 Lyhyt kuvaus tavarahankintojen tai palvelujen luonteesta ja määrästä) ~ dd",
                "dt:contains(II.1.5 Lyhyt kuvaus) ~ dd",
                "dt:contains(II.1.4 Sopimuksen tai hankinnan \\(hankintojen\\) lyhyt kuvaus) ~ dd"
        ));

        content.select(noticeDescriptionSelector).stream()
                .map(Element::text).findFirst().ifPresent(builder::noticeDescription);

        return builder;
    }

}
