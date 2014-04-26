package io.mikael.api.hilma.service;

import io.mikael.api.hilma.domain.NoticeDao;
import io.mikael.api.hilma.domain.ScrapedLink;
import io.mikael.api.hilma.domain.ScrapedNotice;
import io.mikael.api.hilma.scraper.SiteScraper;
import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    }

}
