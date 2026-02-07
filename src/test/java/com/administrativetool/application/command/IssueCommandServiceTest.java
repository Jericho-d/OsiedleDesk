package com.administrativetool.application.command;

import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.event.IssueCreatedEvent;
import com.administrativetool.domain.event.IssueDeletedEvent;
import com.administrativetool.domain.event.IssueUpdatedEvent;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import com.administrativetool.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueCommandServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private IssueQueryService queryService;

    @InjectMocks
    private IssueCommandService commandService;

    @Test
    void create_shouldPublishIssueCreatedEvent() {
        var issue = Issue.builder()
            .title("New Issue")
            .description("Description")
            .status(Status.PREPARED)
            .priority(Priority.HIGH)
            .assignee("user1")
            .send(false)
            .build();

        commandService.create(issue);

        verify(eventPublisher).publishEvent(any(IssueCreatedEvent.class));
    }

    @Test
    void update_shouldPublishIssueUpdatedEventWhenIssueExists() {
        var existing = Issue.builder()
            .id(1L)
            .title("Old Title")
            .description("Old Description")
            .status(Status.PREPARED)
            .priority(Priority.LOW)
            .assignee("user1")
            .send(false)
            .build();

        var updated = Issue.builder()
            .title("Updated Title")
            .description("Updated Description")
            .status(Status.RESOLVED)
            .priority(Priority.CRITICAL)
            .assignee("user2")
            .send(true)
            .build();

        when(queryService.findById(1L)).thenReturn(existing);

        commandService.update(1L, updated);

        verify(queryService).findById(1L);
        verify(eventPublisher).publishEvent(any(IssueUpdatedEvent.class));
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionWhenIssueNotExists() {
        var updated = Issue.builder()
            .title("Updated Title")
            .description("Updated Description")
            .status(Status.RESOLVED)
            .priority(Priority.CRITICAL)
            .assignee("user2")
            .send(true)
            .build();

        when(queryService.findById(999L)).thenThrow(new ResourceNotFoundException("Issue", 999L));

        assertThatThrownBy(() -> commandService.update(999L, updated))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Issue not found with id: 999");

        verify(queryService).findById(999L);
    }

    @Test
    void delete_shouldPublishIssueDeletedEventWhenIssueExists() {
        var existing = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Description")
            .status(Status.PREPARED)
            .priority(Priority.MEDIUM)
            .assignee("user1")
            .send(false)
            .build();

        when(queryService.findById(1L)).thenReturn(existing);

        commandService.delete(1L);

        verify(queryService).findById(1L);
        verify(eventPublisher).publishEvent(any(IssueDeletedEvent.class));
    }

    @Test
    void delete_shouldThrowResourceNotFoundExceptionWhenIssueNotExists() {
        when(queryService.findById(999L)).thenThrow(new ResourceNotFoundException("Issue", 999L));

        assertThatThrownBy(() -> commandService.delete(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Issue not found with id: 999");

        verify(queryService).findById(999L);
    }
}
