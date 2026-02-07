package com.administrativetool.service;

import com.administrativetool.domain.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.company.email}")
    private String adminCompanyEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendIssueEmail(Issue issue) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adminCompanyEmail);
        message.setSubject("Issue Report: " + issue.getTitle());
        message.setText(buildEmailBody(issue));
        mailSender.send(message);
    }

    private String buildEmailBody(Issue issue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Issue Details:\n\n");
        sb.append("Title: ").append(issue.getTitle()).append("\n");
        sb.append("Description: ").append(issue.getDescription()).append("\n");
        sb.append("Priority: ").append(issue.getPriority()).append("\n");
        sb.append("Status: ").append(issue.getStatus()).append("\n");
        sb.append("Assignee: ").append(issue.getAssignee() != null ? issue.getAssignee() : "Unassigned").append("\n");
        sb.append("\nPlease review and process this issue.");
        return sb.toString();
    }
}