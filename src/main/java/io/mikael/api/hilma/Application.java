package io.mikael.api.hilma;

import io.mikael.api.hilma.domain.Notice;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackageClasses=Notice.class)
public class Application {

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().
                    withUser("greg").password("turnquist").roles("USER").and().
                    withUser("ollie").password("gierke").roles("USER", "ADMIN");
        }
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http.csrf().disable().httpBasic();
        }
    }

    public static void main(final String... args) throws Exception {
        new SpringApplicationBuilder(Application.class)
                .registerShutdownHook(true)
                .build()
                .run(args);
    }

}
