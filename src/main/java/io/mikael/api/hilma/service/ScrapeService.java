package io.mikael.api.hilma.service;

import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.domain.ScrapedNotice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScrapeService {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapeService.class);

    private static final Pattern CPV_PATTERN = Pattern.compile("\\(([0-9\\-]*)\\)");

    private static final Pattern TITLE_PATTERN = Pattern.compile("([IVXLCDM]*\\.[0-9]*(?:\\.[0-9]*)) (.*)");

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            "d.M.y H.m", "d.M.y H:m", "d.M.y", "d.M.y 'klo' H.m"
    ).stream().map(DateTimeFormatter::ofPattern).collect(Collectors.toList());

    /**
     * HILMA has several suprising alternative date formats.
     */
    public static Optional<LocalDateTime> parseLocalDateTime(final String input) {
        if (input == null || input.length() == 0) {
            return Optional.empty();
        }
        for (final DateTimeFormatter f : FORMATTERS) {
            try {
                return Optional.of(LocalDateTime.parse(input, f));
            } catch (final DateTimeParseException e) {
                // ignore
            }
        }
        return Optional.empty();
    }

    private static String findCode(final String text) {
        final Matcher m = CPV_PATTERN.matcher(text);
        m.find();
        return m.group(1);
    }

    /**
     * Parse a list of new scraped links out of a HTML InputStream.
     */
    public static List<ScrapedLink> parseNewLinks(final InputStream is) throws IOException {
        final List<ScrapedLink> ret = new ArrayList<>();
        final Document doc = Jsoup.parse(is, "UTF-8", "");
        for (final Element e : doc.select("tr:has(td)")) {
            final Elements data = e.select("td");
            if (data.size() == 4) {
                final Element linkElement = data.get(3).children().first();
                final ScrapedLink.Builder bld = ScrapedLink.builder()
                        .link(linkElement.attr("href"))
                        .name(linkElement.text())
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
    public static Optional<ScrapedNotice> parseNotice(final InputStream is, final String link) throws IOException {
        final Document doc = Jsoup.parse(is, "UTF-8", link);
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        // link, html
        final ScrapedNotice.Builder builder = ScrapedNotice.builder().html(doc.outerHtml()).link(link);

        final Element content = doc.select("div#mainContent").first();

        // id
        doc.select("form#login").stream()
                .map(e -> e.attr("action"))
                .map((String a) -> a.substring(50, a.length() - 1))
                .findFirst()
                .ifPresent(builder::id);

        // published
        content.select("div#datePublished").stream()
                .map(Element::text)
                .map(ScrapeService::parseLocalDateTime)
                .findFirst()
                .ifPresent(o -> o.ifPresent(builder::published));

        // closes
        final String closesSelector = String.join(",", Arrays.asList(
                "dt:contains(Tarjoukset tai osallistumishakemukset on toimitettava hankintayksikölle viimeistään) ~ dd",
                "dt:contains(IV.3.4 Tarjousten vastaanottamisen määräaika) ~ dd",
                "dt:contains(IV.3.4 Osallistumishakemusten vastaanottamisen määräaika) ~ dd"
        ));

        content.select(closesSelector).stream()
                .map(Element::text).map(ScrapeService::parseLocalDateTime)
                .findFirst().ifPresent(o -> o.ifPresent(builder::closes));

        // note
        content.select("div.note").stream()
                .map(Element::text).findFirst().ifPresent(builder::note);

        // mainCpvCode
        final Elements cpvTable = content.select("table.CPV:has(tr > td > strong)");
        cpvTable.select("tr + tr > td").stream()
                .map(Element::text)
                .map(ScrapeService::findCode)
                .findFirst()
                .ifPresent(builder::mainCpvCode);

        content.select("dt:contains(Yhteinen hankintanimikkeistö \\(CPV\\): Pääkohde) ~ dd").stream()
                .map(Element::text)
                .map(ScrapeService::findCode)
                .findFirst()
                .ifPresent(builder::mainCpvCode);

        // type, noticeName
        final List<String> s = content.select("h2").stream()
                .map(e -> e.childNodes())
                .flatMap(l -> l.stream())
                .map(Node::toString)
                .map(String::trim)
                .collect(Collectors.toList());

        builder.type(s.get(0).substring(0, s.get(0).length() - 1));
        builder.noticeName(s.get(2));

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

        // organizationName

        final String organizationNameSelector = String.join(",", Arrays.asList(
                "dt:contains(I.1 Nimi, osoite ja yhteyspiste) + dd td:contains(Virallinen nimi) + td",
                "dt:contains(Hankintayksikön yhteystiedot) + dd td:contains(Hankintayksikkö) + td"
        ));

        content.select(organizationNameSelector).stream()
                .map(Element::text).findFirst().ifPresent(builder::organizationName);

        return Optional.of(builder.build());
    }


    @Value("${urls.new:http://www.hankintailmoitukset.fi/fi/?all=1}")
    private String newListUrl = "http://www.hankintailmoitukset.fi/fi/?all=1";

//    @Scheduled()
    public void scrapeNewList() throws IOException {
    }



}
