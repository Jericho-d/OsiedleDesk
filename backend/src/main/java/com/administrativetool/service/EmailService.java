package com.administrativetool.service;

import com.administrativetool.config.AdminConfig;
import com.administrativetool.domain.model.Attachment;
import com.administrativetool.domain.model.Issue;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
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
            final var mimeMessage = mailSender.createMimeMessage();
            final var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(adminConfig.getCompanyEmail());
            helper.setSubject(buildSubject(issue));
            helper.setText(buildEmailBody(issue));

            if (attachments != null) {
                for (final var attachment : attachments) {
                    helper.addAttachment(
                            attachment.getFilename(),
                            new ByteArrayResource(attachment.getData()),
                            attachment.getContentType()
                    );
                }
            }

            mailSender.send(mimeMessage);
            log.info("Email sent successfully for issue ID: {}", issue.getId());
        } catch (MessagingException e) {
            log.error("Failed to send email for issue ID: {}: {}", issue.getId(), e.getMessage());
            throw new RuntimeException("Failed to send email for issue: " + issue.getId(), e);
        }
    }

    private String buildSubject(Issue issue) {
        return "Issue Report: " + issue.getTitle();
    }

    private String buildEmailBody(Issue issue) {
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

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
