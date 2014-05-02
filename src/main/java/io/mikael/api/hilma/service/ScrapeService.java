package io.mikael.api.hilma.service;

import io.mikael.api.hilma.domain.Notice;
import io.mikael.api.hilma.domain.NoticeDao;
import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.scraper.SiteScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ScrapeService {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapeService.class);

    @Autowired
    private SimpMessagingTemplate webStomp;

    @Autowired
    private NoticeDao noticeDao;

    @Value("${urls.new:http://www.hankintailmoitukset.fi/fi/}")
    private String newListUrl;

    @Value("${urls.search:http://www.hankintailmoitukset.fi/fi/notice/search/}")
    private String searchUrl;

    /** Jsoup parses approximately according to this. */
    private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36";

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("d.M.y");

    @Scheduled(fixedRate=600000L, initialDelay=60000L)
    public void fetchNewNotices() throws IOException {
        final Document doc = Jsoup.connect(newListUrl)
                .userAgent(userAgent).followRedirects(false)
                .get();
        for (final ScrapedLink l : SiteScraper.scrapeLinks(doc)) {
            if (noticeDao.findOne(l.getId()) == null) {
                final Notice notice = fetchNotice(l.getLink());
                noticeDao.save(notice);
                webStomp.convertAndSend("/topic/hilma.foo", notice);
            }
        }
    }

    private Notice fetchNotice(final String link) throws IOException {
        final Document doc = Jsoup.connect(link)
                .userAgent(userAgent).followRedirects(false).get();
        return SiteScraper.scrapeNotice(doc).build();
    }

    public void fetchNotices(final LocalDate from, final LocalDate to) throws IOException {
        final Document doc = Jsoup.connect(searchUrl)
                .userAgent(userAgent).followRedirects(false)
                .data("_s[_sent]", "1")
                .data("_s[published_start]", DD_MM_YYYY.format(from))
                .data("_s[published_end]", DD_MM_YYYY.format(to))
                .data("all", "1")
                .get();
        for (final ScrapedLink l : SiteScraper.scrapeLinks(doc)) {
            if (noticeDao.findOne(l.getId()) == null) {
                noticeDao.save(fetchNotice(l.getLink()));
            }
        }
    }

}
