package com.administrativetool.service;

import com.administrativetool.config.AdminConfig;
import com.administrativetool.config.GmailConfig;
import com.administrativetool.config.GmailServiceProvider;
import com.administrativetool.domain.model.Attachment;
import com.administrativetool.domain.model.Issue;
import com.google.api.services.gmail.model.Message;
import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final GmailServiceProvider gmailServiceProvider;
    private final GmailConfig gmailConfig;
    private final AdminConfig adminConfig;

    public void sendIssueEmail(
            final Issue issue,
            final List<Attachment> attachments
    ) {
        log.info(
                "Sending email for issue ID: {} to {} with {} attachment(s)",
                issue.getId(), adminConfig.getCompanyEmail(),
                attachments != null ? attachments.size() : 0
        );

        try {
            final var mimeMessage = buildMimeMessage(issue, attachments);
            final var gmailMessage = encodeToGmailMessage(mimeMessage);

            final var gmailService = gmailServiceProvider.getGmailService();
            gmailService.users().messages().send("me", gmailMessage).execute();

            log.info("Email sent successfully for issue ID: {}", issue.getId());
        } catch (MessagingException | IOException e) {
            log.error("Failed to send email for issue ID: {}: {}", issue.getId(), e.getMessage());
            throw new RuntimeException("Failed to send email for issue: " + issue.getId(), e);
        }
    }

    private MimeMessage buildMimeMessage(
            final Issue issue,
            final List<Attachment> attachments
    ) throws MessagingException {
        final var session = Session.getDefaultInstance(new Properties());
        final var email = new MimeMessage(session);

        email.setFrom(new InternetAddress(gmailConfig.getSenderEmail()));
        email.addRecipient(
                jakarta.mail.Message.RecipientType.TO,
                new InternetAddress(adminConfig.getCompanyEmail())
        );
        email.setSubject(buildSubject(issue));

        final var multipart = new MimeMultipart();

        final var textPart = new MimeBodyPart();
        textPart.setContent(buildEmailBody(issue), "text/plain; charset=UTF-8");
        multipart.addBodyPart(textPart);

        if (attachments != null) {
            for (final var attachment : attachments) {
                final var attachmentPart = new MimeBodyPart();
                final var dataSource = new ByteArrayDataSource(attachment.getData(), attachment.getContentType());
                attachmentPart.setDataHandler(new DataHandler(dataSource));
                attachmentPart.setFileName(attachment.getFilename());
                multipart.addBodyPart(attachmentPart);
            }
        }

        email.setContent(multipart);
        return email;
    }

    private Message encodeToGmailMessage(final MimeMessage mimeMessage) throws MessagingException, IOException {
        final var buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        final var encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.toByteArray());

        final var message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private String buildSubject(final Issue issue) {
        return "Issue Report: " + issue.getTitle();
    }

    private String buildEmailBody(final Issue issue) {
        return """
                === ISSUE REPORT ===
                Issue ID: %s
                Title: %s
                
                === DESCRIPTION ===
                %s
                
                === METADATA ===
                Created: %s
                """.formatted(issue.getId(), issue.getTitle(), issue.getDescription(), formatDate(issue.getCreatedAt()));
    }

    private String formatDate(final LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
