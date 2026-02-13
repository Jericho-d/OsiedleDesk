package com.administrativetool.application.handler;

import com.administrativetool.domain.event.IssueCreatedEvent;
import com.administrativetool.domain.event.IssueDeletedEvent;
import com.administrativetool.domain.event.IssueUpdatedEvent;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import com.administrativetool.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueCommandHandlerTest {

    @Mock
    private IssueRepository repository;

    @InjectMocks
    private IssueCommandHandler commandHandler;

    @Test
    void handleIssueCreated_shouldSaveIssueToRepository() {
        var issue = Issue.builder()
            .id(1L)
            .title("New Issue")
            .description("Description")
            .status(Status.PREPARED)
            .priority(Priority.HIGH)
            .assignee("user1")
            .sent(false)
            .build();

        when(repository.save(issue)).thenReturn(issue);

        commandHandler.handleIssueCreated(new IssueCreatedEvent(this, issue));

        verify(repository).save(issue);
    }

    @Test
    void handleIssueUpdated_shouldUpdateExistingIssueInRepository() {
        var existing = Issue.builder()
            .id(1L)
            .title("Old Title")
            .description("Old Description")
            .status(Status.PREPARED)
            .priority(Priority.LOW)
            .assignee("user1")
            .sent(false)
            .build();

        var updated = Issue.builder()
            .id(1L)
            .title("Updated Title")
            .description("Updated Description")
            .status(Status.RESOLVED)
            .priority(Priority.CRITICAL)
            .assignee("user2")
            .sent(true)
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Issue.class))).thenReturn(updated);

        commandHandler.handleIssueUpdated(new IssueUpdatedEvent(this, updated));

        verify(repository).findById(1L);
        verify(repository).save(argThat(saved ->
            saved.getId().equals(1L) &&
            saved.getTitle().equals("Updated Title") &&
            saved.getDescription().equals("Updated Description") &&
            saved.getStatus() == Status.RESOLVED &&
            saved.getPriority() == Priority.CRITICAL &&
            saved.getAssignee().equals("user2") &&
            saved.isSent() == true
        ));
    }

    @Test
    void handleIssueUpdated_shouldNotUpdateWhenIssueNotExists() {
        var updated = Issue.builder()
            .id(999L)
            .title("Updated Title")
            .description("Updated Description")
            .status(Status.RESOLVED)
            .priority(Priority.CRITICAL)
            .assignee("user2")
            .sent(true)
            .build();

        when(repository.findById(999L)).thenReturn(Optional.empty());

        commandHandler.handleIssueUpdated(new IssueUpdatedEvent(this, updated));

        verify(repository).findById(999L);
        verify(repository, never()).save(any(Issue.class));
    }

    @Test
    void handleIssueDeleted_shouldDeleteIssueFromRepository() {
        commandHandler.handleIssueDeleted(new IssueDeletedEvent(this, 1L));

        verify(repository).deleteById(1L);
    }
}