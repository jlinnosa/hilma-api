package io.mikael.api.hilma;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    @Configuration
    public static class DataRestConfiguration extends RepositoryRestMvcConfiguration {
    }

    public static void main(final String ... args) throws Exception {
        new SpringApplicationBuilder(Application.class)
                .registerShutdownHook(true)
                .build()
                .run(args);
    }

}
