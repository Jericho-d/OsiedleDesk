package com.administrativetool.application.handler;

import com.administrativetool.domain.event.IssueCreatedEvent;
import com.administrativetool.domain.event.IssueDeletedEvent;
import com.administrativetool.domain.event.IssueUpdatedEvent;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueCommandHandler {
    private final IssueRepository repository;

    @EventListener
    public void handleIssueCreated(IssueCreatedEvent event) {
        repository.save(event.getIssue());
    }

    @EventListener
    public void handleIssueUpdated(IssueUpdatedEvent event) {
        Issue updatedIssue = event.getIssue();
        Issue existing = repository.findById(updatedIssue.getId()).orElse(null);
        if (existing != null) {
            Issue merged = Issue.builder()
                .id(existing.getId())
                .title(updatedIssue.getTitle())
                .description(updatedIssue.getDescription())
                .status(updatedIssue.getStatus())
                .assignee(updatedIssue.getAssignee())
                .sent(updatedIssue.isSent())
                .build();
            repository.save(merged);
        }
    }

    @EventListener
    public void handleIssueDeleted(IssueDeletedEvent event) {
        repository.deleteById(event.getIssueId());
    }
}