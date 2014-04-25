package io.mikael.api.hilma;

import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.domain.ScrapedNotice;
import io.mikael.api.hilma.service.ScrapeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
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

    @Value("classpath:www/all.html")
    private Resource allHtml;

    @Value("classpath:www/posts/2014-011132.html")
    private Resource detailHtml;

    @Value("classpath:www/posts")
    private Resource postsDirectory;

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
        Arrays.stream(postsDirectory.getFile().listFiles())
                .forEach(f -> {
                    try (final InputStream fis = new FileInputStream(f)) {
                        ScrapeService.parseNotice(fis, "/fi/notice/view/2014-011132/").ifPresent(sn -> {
                            if (sn.closes == null) {
                                LOG.debug(f.getName() + " " + sn.type + " " + sn.published + " " + sn.noticeName);
                            }
                        });
                    } catch (final IOException e) {
                        // ignore
                    }
                });
    }

}
