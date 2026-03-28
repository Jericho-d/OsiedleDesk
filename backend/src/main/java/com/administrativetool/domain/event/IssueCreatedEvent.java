package com.administrativetool.domain.event;

import com.administrativetool.domain.model.Issue;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IssueCreatedEvent extends ApplicationEvent {
    private final Issue issue;

    @Builder
    public IssueCreatedEvent(Object source, Issue issue) {
        super(source);
        this.issue = issue;
    }
}
