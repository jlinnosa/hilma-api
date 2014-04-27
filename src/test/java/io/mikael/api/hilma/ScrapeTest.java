package io.mikael.api.hilma;

import io.mikael.api.hilma.domain.NoticeDao;
import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.domain.Notice;
import io.mikael.api.hilma.scraper.SiteScraper;
import io.mikael.api.hilma.service.ScrapeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ScrapeTest {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapeTest.class);

    @Value("classpath:www/all.html")
    private Resource allHtml;

    @Value("classpath:www/posts")
    private Resource postsDirectory;

    @Autowired
    private ScrapeService scrapeService;

    @Autowired
    private SiteScraper siteScraper;

    @Autowired
    private NoticeDao dao;

    @Test
    public void testListScrape() throws IOException {
        final Document doc = Jsoup.parse(allHtml.getInputStream(), "UTF-8", "");
        final List<ScrapedLink> links = SiteScraper.parseNewLinks(doc);
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
        Arrays.stream(postsDirectory.getFile().listFiles())
                .forEach(f -> {
                    try (final InputStream fis = new FileInputStream(f)) {
                        final Document doc = Jsoup.parse(fis, "UTF-8", "/fi/notice/view/2014-011132/");
                        final Notice notice = SiteScraper.parseNotice(doc).build();
                        if (notice.getCloses() == null) {
                            LOG.debug(notice.getId() + " " + notice.getType() + " " + notice.getPublished()
                                    + " " + notice.getNoticeName());
                        }
                        dao.save(notice);
                    } catch (final IOException e) {
                        // ignore
                    }
                });
        LOG.debug("from dao: " + dao.findOne("2014-012067"));
    }

}
