package com.administrativetool.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttlsEnable;

    @Bean
    public JavaMailSender javaMailSender() {
        final var mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        final var props = mailSender.getJavaMailProperties();
        props.putAll(Map.of(
                "mail.smtp.auth", smtpAuth,
                "mail.smtp.starttls.enable", starttlsEnable,
                "mail.transport.protocol", "smtp",
                "mail.smtp.connectiontimeout", "5000",
                "mail.smtp.timeout", "3000",
                "mail.smtp.writetimeout", "5000"
        ));
        return mailSender;
    }
}