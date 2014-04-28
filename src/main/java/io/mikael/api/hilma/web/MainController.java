package io.mikael.api.hilma.web;

import io.mikael.api.hilma.service.ScrapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.*;
import java.io.IOException;

@Controller
public class MainController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ScrapeService ss;

    /**
     * We want every request/response pair to be handled as UTF-8, and every response identify itself as UTF-8.
     */
    @Bean
    public Filter characterSetFilter() {
        return (LambdaFilter) (req, res, chain) -> {
            req.setCharacterEncoding("UTF-8");
            res.setCharacterEncoding("UTF-8");
            chain.doFilter(req, res);
        };
    }

    @RequestMapping("/")
    public ResponseEntity<String> welcome() {
        return new ResponseEntity<>("Hello!", HttpStatus.OK);
    }

    @RequestMapping("/fetch")
    public ResponseEntity<String> fetch() throws IOException {
        ss.fetchNewNotices();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
    }

    @RequestMapping("/message")
    public ResponseEntity<String> sendMessage(final @RequestParam("msg") String msg) {
        template.convertAndSend("/topic/hilma.foo", msg);
        return new ResponseEntity<>("OK " + msg.length(), HttpStatus.OK);
    }

}
