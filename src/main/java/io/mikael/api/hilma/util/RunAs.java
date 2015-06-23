package io.mikael.api.hilma.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

@Slf4j
public class RunAs {

    @FunctionalInterface
    public interface RunWithException {
        void run() throws Exception;
    }

    public static void runAsAdmin(final RunWithException func) {
        final Authentication admin = new AnonymousAuthenticationToken(
                "system", "system", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        final Authentication original = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(admin);
        try {
            func.run();
        } catch (Exception e) {
            log.error("Run as method failed", e);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(original);
        }
    }

}
