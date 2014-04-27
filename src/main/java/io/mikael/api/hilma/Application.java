package io.mikael.api.hilma;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableScheduling
public class Application {

    @Profile("heroku")
    @Configuration
    @ServiceScan
    public static class CloudConfiguration extends AbstractCloudConfig {
    }

    @Configuration
    public static class DataRestConfiguration extends RepositoryRestMvcConfiguration {
    }

    @Bean
    public Module javaTimeJsonModule() {
        return new JSR310Module();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
