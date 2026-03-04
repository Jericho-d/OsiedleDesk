package com.administrativetool.service;

import com.administrativetool.domain.model.Status;
import com.administrativetool.repository.AttachmentRepository;
import com.administrativetool.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueEmailService {

    private final IssueRepository issueRepository;
    private final AttachmentRepository attachmentRepository;
    private final EmailService emailService;

    @Transactional
    public void sendIssueToAdmin(Long issueId) {
        log.info("Processing send request for issue ID: {}", issueId);

        final var issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        if (issue.isSent()) {
            log.info("Issue {} was already sent at {}", issueId, issue.getSentAt());
            return;
        }

        // Load attachments for the issue
        final var attachments = attachmentRepository.findByIssueId(issueId);
        log.info("Found {} attachment(s) for issue {}", attachments.size(), issueId);

        // Send the email first — if it fails, exception propagates and transaction rolls back
        emailService.sendIssueEmail(issue, attachments);

        // Only update status after successful send
        issue.setStatus(Status.IN_PROGRESS);
        issue.setSent(true);
        issue.setSentAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());

        issueRepository.save(issue);
        log.info("Issue {} marked as sent and moved to IN_PROGRESS", issueId);
    }
}
