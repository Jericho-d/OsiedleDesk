package com.administrativetool.service;

import com.administrativetool.domain.dto.AttachmentResponse;
import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.domain.model.Attachment;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import com.administrativetool.repository.AttachmentRepository;
import com.administrativetool.repository.IssueRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public IssueResponse createIssue(
        final IssueCreateRequest request,
        final Long creatorId
    ) {
        return createIssue(request, creatorId, null);
    }

    @Transactional
    public IssueResponse createIssue(
        final IssueCreateRequest request,
        final Long creatorId,
        final List<MultipartFile> files
    ) {
        final var issue = Issue.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(Status.PREPARED)
            .creatorId(creatorId)
            .sent(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        final var savedIssue = issueRepository.save(issue);

        if (files != null && !files.isEmpty()) {
            for (final var file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                try {
                    final var attachment = Attachment.builder()
                        .issueId(savedIssue.getId())
                        .filename(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .data(file.getBytes())
                        .size(file.getSize())
                        .createdAt(LocalDateTime.now())
                        .build();
                    attachmentRepository.save(attachment);
                } catch (IOException e) {
                    log.error(
                        "Failed to save attachment '{}' for issue {}: {}",
                        file.getOriginalFilename(), savedIssue.getId(), e.getMessage()
                    );
                    throw new RuntimeException("Failed to save attachment: " + file.getOriginalFilename(), e);
                }
            }
        }

        return mapToResponse(savedIssue);
    }

    public List<IssueResponse> getAllIssues() {
        return issueRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public IssueResponse getIssueById(Long id) {
        return issueRepository.findById(id)
            .map(this::mapToResponse)
            .orElse(null);
    }

    public List<IssueResponse> getIssuesByStatus(Status status) {
        return issueRepository.findByStatusOrderByCreatedAtDesc(status)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public IssueResponse updateIssueStatus(Long id, Status newStatus) {
        final var issue = issueRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + id));

        validateStatusTransition(issue.getStatus());

        final var updatedIssue = Issue.builder()
            .id(issue.getId())
            .title(issue.getTitle())
            .description(issue.getDescription())
            .status(newStatus)
            .assignee(issue.getAssignee())
            .creatorId(issue.getCreatorId())
            .sent(issue.isSent())
            .sentAt(issue.getSentAt())
            .createdAt(issue.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        final var saved = issueRepository.save(updatedIssue);
        return mapToResponse(saved);
    }

    private void validateStatusTransition(final Status currentStatus) {
        if (currentStatus == Status.PREPARED) {
            throw new IllegalArgumentException(
                "Issues in PREPARED status can only be advanced via the Send Email flow.");
        }
    }

    private IssueResponse mapToResponse(final Issue issue) {
        final var attachments = attachmentRepository.findByIssueId(issue.getId())
            .stream()
            .map(a -> AttachmentResponse.builder()
                .id(a.getId())
                .issueId(a.getIssueId())
                .filename(a.getFilename())
                .contentType(a.getContentType())
                .size(a.getSize())
                .build())
            .toList();

        return IssueResponse.builder()
            .id(issue.getId())
            .title(issue.getTitle())
            .description(issue.getDescription())
            .status(issue.getStatus())
            .assignee(issue.getAssignee())
            .sent(issue.isSent())
            .creatorId(issue.getCreatorId())
            .createdAt(issue.getCreatedAt())
            .updatedAt(issue.getUpdatedAt())
            .attachments(attachments)
            .build();
    }
}
