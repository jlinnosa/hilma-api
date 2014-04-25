package io.mikael.api.hilma;

import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.domain.ScrapedNotice;
import io.mikael.api.hilma.service.ScrapeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ScrapeTest {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapeTest.class);

    private static final Pattern CPV_PATTERN = Pattern.compile("\\(([0-9\\-]*)\\)");

    private static final Pattern TITLE_PATTERN = Pattern.compile("([IVXLCDM]*\\.[0-9]*(?:\\.[0-9]*)) (.*)");

    @Value("classpath:www/all.html")
    private Resource allHtml;

    @Value("classpath:www/posts/2014-011132.html")
    private Resource detailHtml;

    @Test
    public void testListScrape() throws IOException {
        final List<ScrapedLink> links = ScrapeService.parseNewLinks(allHtml.getInputStream());
        assertEquals(links.size(), 3161);
        for (final ScrapedLink l : links) {
            assertNotNull(l.published);
            assertNotNull(l.name);
            assertNotNull(l.type);
            assertNotNull(l.link);
        }
    }

    public static <T> boolean fillIf(final Supplier<T> supplier, final Consumer<T> consumer) {
        return fillIf(supplier, consumer, null);
    }

    public static <T> boolean fillIf(final Supplier<? extends T> supplier, final Consumer<T> consumer, final Consumer<Exception> exceptionHandler) {
        try {
            final T data = supplier.get();
            if (null == data) {
                return false;
            }
            consumer.accept(data);
            return true;
        } catch (final Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            }
            return false;
        }
    }

    private static String findCode(final String text) {
        final Matcher m = CPV_PATTERN.matcher(text);
        m.find();
        return m.group(1);
    }

    private static Optional<Matcher> ifFound(final Pattern pattern, final String text, final Consumer<Matcher> consumer) {
        final Matcher m = pattern.matcher(text);
        if (m.find()) {
            consumer.accept(m);
            return Optional.of(m);
        }
        return Optional.empty();
    }

    private static <T> Optional<T> transform(final Pattern pattern, final String text, final Function<Matcher, T> transformer) {
        final Matcher m = pattern.matcher(text);
        if (m.find()) {
            return Optional.of(transformer.apply(m));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<ScrapedNotice> parseNotice(final InputStream is, final String link) throws IOException {
        final Document doc = Jsoup.parse(is, "UTF-8", link);

        // link, html
        final ScrapedNotice.Builder builder = ScrapedNotice.builder()/*.html(doc.outerHtml())*/.link(link);

        final Element content = doc.select("div#mainContent").first();

        // id

        // published

        // closes

        // note
        content.select("div.note").stream()
                .map(Element::text).findFirst().ifPresent(builder::note);

        // mainCpvCode
        final Elements es = content.select("table.CPV:has(tr > td > strong)");
        es.select("tr + tr > td").stream()
                .map(Element::text)
                .map(ScrapeTest::findCode)
                .findFirst()
                .ifPresent(builder::mainCpvCode);

        // type, noticeName
        final List<String> s = content.select("h2").stream()
                .map(e -> e.childNodes())
                .flatMap(l -> l.stream())
                .map(Node::toString)
                .map(String::trim)
                .collect(Collectors.toList());

        builder.type(s.get(0));
        builder.noticeName(s.get(2));

        // noticeDescription
        content.select("dt:contains(II.1.4) ~ dd").stream()
                .map(Element::text)
                .findFirst()
                .ifPresent(builder::noticeDescription);

        // type = Kansallinen hankintailmoitus
        content.select("dt:contains(Hankinnan kuvaus) ~ dd").stream()
                .map(Element::text)
                .findFirst()
                .ifPresent(builder::noticeDescription);


        // organizationName

        return Optional.of(builder.build());
    }

    @Test
    public void testMap() throws Exception {
        Arrays.asList("1", "2", "3").stream()
                .map(Object::toString)
                .map(s -> null)
                .forEach(System.out::println);
    }

    @Test
    public void testDetailScrape2() throws Exception {
        final Optional<ScrapedNotice> sn = parseNotice(detailHtml.getInputStream(), "http://www.hankintailmoitukset.fi/fi/notice/view/2014-011132/");
        LOG.debug(sn.toString());
    }


    @Test
    public void testDetailScrape() throws Exception {
        final Document doc = Jsoup.parse(detailHtml.getInputStream(), "UTF-8", "http://www.hankintailmoitukset.fi/fi/notice/view/2014-011132/");

        final Element content = doc.select("div#mainContent").first();

        LOG.debug(content.select("h2").first().childNodes().toString());

        content.select("h2").first().childNodes().stream().forEach(n -> LOG.debug(n.toString()));

        final List<Node> s = content.select("h2").stream()
                .map(Node::childNodes)
                .flatMap(l -> l.stream())
                .collect(Collectors.toList());

        final Elements notes = content.select("div.note");
        if (!notes.isEmpty()) {
            LOG.debug("note: " + notes.first().text());
        }

        for (final Element e : content.select("dt")) {
            final Matcher m = TITLE_PATTERN.matcher(e.text());
            if (m.find()) {
                LOG.debug(m.group(1));
                LOG.debug(m.group(2));
            }
            final Element dd = e.nextElementSibling();
//            LOG.debug(dd.children().toString());
        }

    }

}
