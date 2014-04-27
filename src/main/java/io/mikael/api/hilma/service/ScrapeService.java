package io.mikael.api.hilma.service;

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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
public class ScrapeService {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapeService.class);

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private SiteScraper scraper;

    @Value("${urls.new:http://www.hankintailmoitukset.fi/fi/?all=1}")
    private String newListUrl;

    /**
     *
     */
    public void fetchNewNotices() throws IOException {
        final Document doc = Jsoup.connect(newListUrl).get();
        for (final ScrapedLink l : SiteScraper.parseNewLinks(doc)) {
            if (noticeDao.findOne(l.getId()) == null) {
                final Document notice = Jsoup.connect(l.getLink()).get();
                noticeDao.save(SiteScraper.parseNotice(notice).build());
            }
        }
    }

}
