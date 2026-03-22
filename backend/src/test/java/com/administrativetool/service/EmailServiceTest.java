package com.administrativetool.service;

import com.administrativetool.config.AdminConfig;
import com.administrativetool.config.GmailConfig;
import com.administrativetool.config.GmailServiceProvider;
import com.administrativetool.domain.model.Attachment;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceTest {

    static final String COMPANY_EMAIL = "some@example.com";
    static final String SENDER_EMAIL = "sender@gmail.com";

    @Mock
    GmailServiceProvider gmailServiceProvider;
    @Mock
    GmailConfig gmailConfig;
    @Mock
    AdminConfig adminConfig;
    @Mock
    Gmail gmail;
    @Mock
    Gmail.Users users;
    @Mock
    Gmail.Users.Messages messages;
    @Mock
    Gmail.Users.Messages.Send send;

    @InjectMocks
    EmailService emailService;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        when(gmailServiceProvider.getGmailService()).thenReturn(gmail);
        when(gmail.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.send(eq("me"), any(Message.class))).thenReturn(send);
        when(send.execute()).thenReturn(new Message().setId("msg-123"));

        when(gmailConfig.getSenderEmail()).thenReturn(SENDER_EMAIL);
        when(adminConfig.getCompanyEmail()).thenReturn(COMPANY_EMAIL);
    }

    @Nested
    @DisplayName("sendIssueEmail")
    class SendIssueEmail {

        @Test
        @SneakyThrows
        @DisplayName("should send email via Gmail API to admin company")
        void should_send_email_via_gmail_api() {
            var issue = Issue.builder()
                    .id(1L)
                    .title("Test Issue")
                    .description("Test Description")
                    .status(Status.PREPARED)
                    .assignee("John Doe")
                    .sent(false)
                    .build();

            emailService.sendIssueEmail(issue, List.of());

            var messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messages).send(eq("me"), messageCaptor.capture());
            verify(send).execute();

            var sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getRaw()).isNotBlank();
        }

        @Test
        @SneakyThrows
        @DisplayName("should handle null attachments list")
        void should_handle_null_attachments() {
            var issue = Issue.builder()
                    .id(1L)
                    .title("Test Issue")
                    .description("Test Description")
                    .status(Status.PREPARED)
                    .assignee(null)
                    .sent(false)
                    .build();

            emailService.sendIssueEmail(issue, null);

            verify(messages).send(eq("me"), any(Message.class));
            verify(send).execute();
        }

        @Test
        @SneakyThrows
        @DisplayName("should include attachments in email")
        void should_include_attachments() {
            var issue = Issue.builder()
                    .id(1L)
                    .title("Issue With Attachments")
                    .description("Description")
                    .status(Status.PREPARED)
                    .assignee("Jane Doe")
                    .sent(false)
                    .build();

            var attachment = Attachment.builder()
                    .filename("test.txt")
                    .contentType("text/plain")
                    .data("Hello World".getBytes())
                    .size(11L)
                    .build();

            emailService.sendIssueEmail(issue, List.of(attachment));

            var messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messages).send(eq("me"), messageCaptor.capture());
            verify(send).execute();

            assertThat(messageCaptor.getValue().getRaw()).isNotBlank();
        }

        @Test
        @SneakyThrows
        @DisplayName("should throw RuntimeException when Gmail API fails")
        void should_throw_when_gmail_api_fails() {
            when(send.execute()).thenThrow(new java.io.IOException("Gmail API error"));

            var issue = Issue.builder()
                    .id(1L)
                    .title("Failing Issue")
                    .description("Description")
                    .status(Status.PREPARED)
                    .assignee("John Doe")
                    .sent(false)
                    .build();

            assertThatThrownBy(() -> emailService.sendIssueEmail(issue, List.of()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send email for issue: 1");
        }
    }
}
