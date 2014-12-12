package io.mikael.api.hilma.web;

import com.google.common.collect.ImmutableMap;
import io.mikael.api.hilma.service.ScrapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.Filter;
import java.io.IOException;
import java.util.Map;

@RestController
public class MainController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ScrapeService ss;

    @RequestMapping("/fetch")
    public ResponseEntity<String> fetch() throws IOException {
        ss.fetchNewNotices();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleException(final WebRequest req, final Exception ex) {
        final Map<String, String> ret = ImmutableMap.of(
                "status", "500",
                "message", ex.getMessage(),
                "path", req.getContextPath(),
                "timestamp", java.time.OffsetDateTime.now().toString());
        return new ResponseEntity<>(ret, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
