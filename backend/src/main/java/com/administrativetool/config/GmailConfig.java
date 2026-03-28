package com.administrativetool.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gmail")
@Getter
@Setter
public class GmailConfig {

    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String senderEmail;
    private String applicationName = "OsiedleDesk";
}
