package com.administrativetool.service;

import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import com.administrativetool.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;

    @Transactional
    public IssueResponse createIssue(IssueCreateRequest request, Long creatorId) {
        final var issue = Issue.builder()
                .title(request.title())
                .description(request.description())
                .status(Status.PREPARED)
                .priority(Priority.valueOf(request.priority()))
                .creatorId(creatorId)
                .sent(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        final var savedIssue = issueRepository.save(issue);
        return mapToResponse(savedIssue);
    }

    public List<IssueResponse> getAllIssues() {
        return issueRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    private IssueResponse mapToResponse(final Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .assignee(issue.getAssignee())
                .sent(issue.isSent())
                .creatorId(issue.getCreatorId())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}
