package com.administrativetool.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final CustomUserDetailsService userDetailsService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        final var username = event.getAuthentication().getName();
        log.info("Successful login for user: {}", username);
        userDetailsService.recordSuccessfulLogin(username);
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        final var username = event.getAuthentication().getPrincipal().toString();
        log.warn("Failed login attempt for user: {}", username);
        userDetailsService.recordFailedLogin(username);
    }
}
