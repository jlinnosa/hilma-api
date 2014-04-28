package io.mikael.api.hilma.service;

import io.mikael.api.hilma.domain.Notice;
import io.mikael.api.hilma.domain.NoticeDao;
import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.scraper.SiteScraper;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
public class ScrapeService {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapeService.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private SiteScraper scraper;

    @Value("${urls.new:http://www.hankintailmoitukset.fi/fi/}")
    private String newListUrl;

    /**
     *
     */
    public void fetchNewNotices() throws IOException {
        final Document doc = Jsoup.connect(newListUrl).get();
        for (final ScrapedLink l : SiteScraper.parseNewLinks(doc)) {
            LOG.info("checking notice <" + l.getId() + ">");
            if (noticeDao.findOne(l.getId()) == null) {
                LOG.info("didn't find it");
                final Document noticePage = Jsoup.connect(l.getLink()).get();
                final Notice notice = SiteScraper.parseNotice(noticePage).build();
                noticeDao.save(notice);
                template.convertAndSend("/topic/hilma.foo", notice);
            }
        }
    }

}
