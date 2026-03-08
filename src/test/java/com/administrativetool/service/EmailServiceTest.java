package com.administrativetool.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.administrativetool.config.AdminConfig;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    static final String COMPANY_EMAIL = "some@example.com";

    @Mock
    JavaMailSender mailSender;
    @Mock
    MimeMessage mimeMessage;
    @Mock
    AdminConfig adminConfig;
    @InjectMocks
    EmailService emailService;

    @Test
    @SneakyThrows
    void sendIssueEmail_shouldSendEmailToAdminCompany() {
        var issue = Issue.builder()
                .id(1L)
                .title("Test Issue")
                .description("Test Description")
                .status(Status.PREPARED)
                .assignee("John Doe")
                .sent(false)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(adminConfig.getCompanyEmail()).thenReturn(COMPANY_EMAIL);

        emailService.sendIssueEmail(issue, List.of());

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendIssueEmail_shouldHandleNullAttachments() {
        var issue = Issue.builder()
                .id(1L)
                .title("Test Issue")
                .description("Test Description")
                .status(Status.PREPARED)
                .assignee(null)
                .sent(false)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(adminConfig.getCompanyEmail()).thenReturn(COMPANY_EMAIL);

        emailService.sendIssueEmail(issue, null);

        verify(mailSender).send(mimeMessage);
    }
}
