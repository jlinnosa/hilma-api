package io.mikael.api.hilma;

import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.service.ScrapeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test
    public void testDetailScrape() throws Exception {
        final Document doc = Jsoup.parse(detailHtml.getInputStream(), "UTF-8", "http://www.hankintailmoitukset.fi/fi/notice/view/2014-011132/");

        final Element content = doc.select("div#mainContent").first();

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
            LOG.debug(dd.children().toString());
        }

        final Elements es = content.select("table.CPV:has(tr > td > strong)");

        LOG.debug(es.toString());

        final Element e2 = es.select("tr ~ tr > td").first();

        LOG.debug(e2.text());

        final Optional<String> code = findCode(e2.text());
        LOG.debug(code.get());
    }

    private static Optional<String> findCode(final String text) {
        final Matcher m = CPV_PATTERN.matcher(text);
        if (m.find()) {
            return Optional.of(m.group(1));
        } else {
            return Optional.empty();
        }
    }

}
