package com.administrativetool.application.query;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import com.administrativetool.exception.ResourceNotFoundException;
import com.administrativetool.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueQueryServiceTest {

    @Mock
    private IssueRepository repository;

    @InjectMocks
    private IssueQueryService queryService;

    @Test
    void findAll_shouldReturnAllIssues() {
        var issue1 = Issue.builder()
                .id(1L)
                .title("Issue 1")
                .description("Description 1")
                .status(Status.PREPARED)
                .assignee("user1")
                .sent(false)
                .build();
        var issue2 = Issue.builder()
                .id(2L)
                .title("Issue 2")
                .description("Description 2")
                .status(Status.RESOLVED)
                .assignee("user2")
                .sent(true)
                .build();

        when(repository.findAll()).thenReturn(java.util.List.of(issue1, issue2));

        var result = queryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(issue1, issue2);
        verify(repository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoIssues() {
        when(repository.findAll()).thenReturn(java.util.List.of());

        var result = queryService.findAll();

        assertThat(result).isEmpty();
        verify(repository).findAll();
    }

    @Test
    void findById_shouldReturnIssueWhenExists() {
        var issue = Issue.builder()
                .id(1L)
                .title("Test Issue")
                .description("Description")
                .status(Status.IN_PROGRESS)
                .assignee("user1")
                .sent(false)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(issue));

        var result = queryService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(issue);
        verify(repository).findById(1L);
    }

    @Test
    void findById_shouldThrowResourceNotFoundExceptionWhenNotExists() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Issue not found with id: 999");

        verify(repository).findById(999L);
    }
}
