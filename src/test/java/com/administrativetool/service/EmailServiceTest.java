package com.administrativetool.service;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendIssueEmail_shouldSendEmailToAdminCompany() {
        var issue = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Test Description")
            .status(Status.PREPARED)
            .priority(Priority.HIGH)
            .assignee("John Doe")
            .sent(false)
            .build();

        when(mailSender.createMimeMessage()).thenReturn(null);

        emailService.sendIssueEmail(issue);

        var messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        var capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getSubject()).isEqualTo("Issue Report: Test Issue");
        assertThat(capturedMessage.getText()).contains("Test Issue");
        assertThat(capturedMessage.getText()).contains("Test Description");
        assertThat(capturedMessage.getText()).contains("HIGH");
        assertThat(capturedMessage.getText()).contains("PREPARED");
        assertThat(capturedMessage.getText()).contains("John Doe");
    }

    @Test
    void sendIssueEmail_shouldHandleNullAssignee() {
        var issue = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Test Description")
            .status(Status.PREPARED)
            .priority(Priority.MEDIUM)
            .assignee(null)
            .sent(false)
            .build();

        emailService.sendIssueEmail(issue);

        var messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        var capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getText()).contains("Unassigned");
    }
}