package com.administrativetool.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailServiceProvider {

    private final GmailConfig gmailConfig;

    public Gmail getGmailService() {
        try {
            final var credentials = UserCredentials.newBuilder()
                    .setClientId(gmailConfig.getClientId())
                    .setClientSecret(gmailConfig.getClientSecret())
                    .setRefreshToken(gmailConfig.getRefreshToken())
                    .build();

            final var scopedCredentials = credentials.createScoped(List.of(GmailScopes.GMAIL_SEND));
            final var requestInitializer = new HttpCredentialsAdapter(scopedCredentials);

            final var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final var jsonFactory = GsonFactory.getDefaultInstance();

            return new Gmail.Builder(httpTransport, jsonFactory, requestInitializer)
                    .setApplicationName(gmailConfig.getApplicationName())
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to create Gmail service: {}", e.getMessage());
            throw new RuntimeException("Failed to create Gmail service", e);
        }
    }
}
