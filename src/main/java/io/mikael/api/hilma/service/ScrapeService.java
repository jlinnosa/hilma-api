package io.mikael.api.hilma.service;

import io.mikael.api.hilma.domain.ScrapedLink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ScrapeService {

    private static final Pattern CPV_PATTERN = Pattern.compile("\\(([0-9\\-]*)\\)");

    private static final Pattern TITLE_PATTERN = Pattern.compile("([IVXLCDM]*\\.[0-9]*(?:\\.[0-9]*)) (.*)");

    private static final DateTimeFormatter DTFM_PUBLISHED = DateTimeFormatter.ofPattern("d.M.y H.m");

    private static final DateTimeFormatter DTFM_CLOSES = DateTimeFormatter.ofPattern("d.M.y H:m");

    private static final DateTimeFormatter DTFM_DATE_ONLY = DateTimeFormatter.ofPattern("d.M.y");

    /**
     * HILMA has several suprising alternative date formats.
     */
    public static Optional<LocalDateTime> parseLocalDateTime(final String input) {
        if (input == null || input.length() == 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.parse(input, DTFM_PUBLISHED));
        } catch (final DateTimeParseException e) {
            // ignore
        }
        try {
            return Optional.of(LocalDateTime.parse(input, DTFM_CLOSES));
        } catch (final DateTimeParseException e) {
            // ignore
        }
        try {
            return Optional.of(LocalDateTime.parse(input, DTFM_DATE_ONLY));
        } catch (final DateTimeParseException e) {
            // ignore
        }
        return Optional.empty();
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
                final ScrapedLink.ScrapedLinkBuilder bld = ScrapedLink.builder()
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


    @Value("${urls.new:http://www.hankintailmoitukset.fi/fi/?all=1}")
    private String newListUrl = "http://www.hankintailmoitukset.fi/fi/?all=1";

//    @Scheduled()
    public void scrapeNewList() throws IOException {
        final Document doc = Jsoup.connect(newListUrl).get();
        final Elements rows = doc.select("tr");
        final String start = rows.select("td[0]").text();
        final String finish = rows.select("td[1]").text();
        final Element d3 = rows.select("td[2]/a").first();
        final String link = d3.attr("href");
        final String text = d3.text();

    }

}
