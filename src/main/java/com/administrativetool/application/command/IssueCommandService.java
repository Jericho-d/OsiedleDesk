package com.administrativetool.application.command;

import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.event.IssueCreatedEvent;
import com.administrativetool.domain.event.IssueDeletedEvent;
import com.administrativetool.domain.event.IssueUpdatedEvent;
import com.administrativetool.domain.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class IssueCommandService {
    private final ApplicationEventPublisher eventPublisher;
    private final IssueQueryService queryService;

    @Autowired
    public IssueCommandService(ApplicationEventPublisher eventPublisher, IssueQueryService queryService) {
        this.eventPublisher = eventPublisher;
        this.queryService = queryService;
    }

    public void create(Issue issue) {
        eventPublisher.publishEvent(new IssueCreatedEvent(this, issue));
    }

    public void update(Long id, Issue updated) {
        queryService.findById(id);
        Issue updatedWithId = Issue.builder()
            .id(id)
            .title(updated.getTitle())
            .description(updated.getDescription())
            .status(updated.getStatus())
            .assignee(updated.getAssignee())
            .sent(updated.isSent())
            .build();
        eventPublisher.publishEvent(new IssueUpdatedEvent(this, updatedWithId));
    }

    public void delete(Long id) {
        queryService.findById(id);
        eventPublisher.publishEvent(new IssueDeletedEvent(this, id));
    }
}
