package com.administrativetool.service;

import com.administrativetool.domain.model.Issue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.company.email}")
    private String adminCompanyEmail;

    @Async("emailExecutor")
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendIssueEmail(Issue issue) {
        log.info("Sending email for issue ID: {} to {}", issue.getId(), adminCompanyEmail);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminCompanyEmail);
            message.setSubject(buildSubject(issue));
            message.setText(buildEmailBody(issue));
            
            mailSender.send(message);
            log.info("Email sent successfully for issue ID: {}", issue.getId());
        } catch (Exception e) {
            log.error("Failed to send email for issue ID: {}", issue.getId(), e);
            throw e;
        }
    }

    private String buildSubject(Issue issue) {
        return String.format("[Issue #%d - %s] %s", 
                issue.getId(), 
                issue.getPriority(), 
                issue.getTitle());
    }

    private String buildEmailBody(Issue issue) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ISSUE REPORT ===\n\n");
        sb.append("Issue ID: ").append(issue.getId()).append("\n");
        sb.append("Title: ").append(issue.getTitle()).append("\n");
        sb.append("Priority: ").append(issue.getPriority()).append("\n");
        sb.append("Status: ").append(issue.getStatus()).append("\n\n");
        
        sb.append("=== DESCRIPTION ===\n");
        sb.append(issue.getDescription()).append("\n\n");
        
        sb.append("=== METADATA ===\n");
        sb.append("Created: ").append(formatDate(issue.getCreatedAt())).append("\n");
        sb.append("Assignee: ").append(issue.getAssignee() != null ? issue.getAssignee() : "Unassigned").append("\n\n");
        
        sb.append("=== ACTION REQUIRED ===\n");
        sb.append("Please review and process this issue.\n");
        sb.append("Access the issue tracker at: http://localhost:8080/board\n");
        
        return sb.toString();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
