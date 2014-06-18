package io.mikael.api.hilma;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableScheduling
public class Application {

    @Configuration
    @Profile("cloud")
    public class CloudConfig extends AbstractCloudConfig {
        @Bean
        public ApplicationInstanceInfo applicationInfo() {
            return cloud().getApplicationInstanceInfo();
        }
        @Bean
        public DataSource dataSource() {
            return connectionFactory().dataSource();
        }
    }

    @Configuration
    public static class DataRestConfiguration extends RepositoryRestMvcConfiguration {
    }

    @Configuration
    @EnableWebSocketMessageBroker
    @NoArgsConstructor
    public static class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.setApplicationDestinationPrefixes("/app")
                    .enableSimpleBroker("/queue", "/topic");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/hilma").withSockJS();
        }
        
    }

    @Bean
    public Module javaTimeJsonModule() {
        return new JSR310Module();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
