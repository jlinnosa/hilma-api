package io.mikael.api.hilma;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@SpringBootApplication
@EnableScheduling
public class Application {

    @Configuration
    public static class DataRestConfiguration extends RepositoryRestMvcConfiguration {
    }

    @Configuration
    @EnableWebSocketMessageBroker
    @NoArgsConstructor
    public static class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(final MessageBrokerRegistry config) {
            config.setApplicationDestinationPrefixes("/app")
                    .enableSimpleBroker("/queue", "/topic");
        }

        @Override
        public void registerStompEndpoints(final StompEndpointRegistry registry) {
            registry.addEndpoint("/hilma").withSockJS();
        }
        
    }

    @Bean
    public Module javaTimeJsonModule() {
        return new JSR310Module();
    }

    public static void main(final String ... args) throws Exception {
        new SpringApplicationBuilder(Application.class)
                .registerShutdownHook(true)
                .build()
                .run(args);
    }
}
