package com.administrativetool.service;

import com.administrativetool.domain.dto.EmailSendResponse;
import com.administrativetool.domain.model.Status;
import com.administrativetool.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueEmailService {

    private final IssueRepository issueRepository;
    private final EmailService emailService;

    @Transactional
    public EmailSendResponse sendIssueToAdmin(Long issueId) {
        log.info("Processing send request for issue ID: {}", issueId);
        
        var issueOptional = issueRepository.findById(issueId);
        if (issueOptional.isEmpty()) {
            log.warn("Issue not found: {}", issueId);
            return EmailSendResponse.builder()
                    .issueId(issueId)
                    .status("NOT_FOUND")
                    .message("Issue not found")
                    .build();
        }
        
        var issue = issueOptional.get();
        
        // Check if already sent (idempotency)
        if (issue.isSent()) {
            log.info("Issue {} was already sent at {}", issueId, issue.getSentAt());
            return EmailSendResponse.builder()
                    .issueId(issueId)
                    .status("ALREADY_SENT")
                    .message("Email was already sent on " + issue.getSentAt())
                    .sentAt(issue.getSentAt())
                    .build();
        }
        
        // Update status and mark as sent
        issue.setStatus(Status.IN_PROGRESS);
        issue.setSent(true);
        issue.setSentAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        
        issueRepository.save(issue);
        
        // Register transaction synchronization to send email after commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Transaction committed, sending email for issue {}", issueId);
                emailService.sendIssueEmail(issue);
            }
            
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.warn("Transaction rolled back for issue {}, email not sent", issueId);
                }
            }
        });
        
        log.info("Issue {} marked for sending, email will be sent after transaction commit", issueId);
        
        return EmailSendResponse.builder()
                .issueId(issueId)
                .status("QUEUED")
                .message("Email queued for delivery")
                .sentAt(issue.getSentAt())
                .build();
    }
}
