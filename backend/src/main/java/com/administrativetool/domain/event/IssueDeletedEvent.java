package com.administrativetool.domain.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IssueDeletedEvent extends ApplicationEvent {
    private final Long issueId;

    @Builder
    public IssueDeletedEvent(Object source, Long issueId) {
        super(source);
        this.issueId = issueId;
    }
}
