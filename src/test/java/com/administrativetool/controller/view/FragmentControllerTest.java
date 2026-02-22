package com.administrativetool.controller.view;

import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import com.administrativetool.service.IssueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FragmentControllerTest {

    @Mock
    IssueService issueService;

    @InjectMocks
    FragmentController fragmentController;

    @Test
    void getIssueDetailFragment_shouldReturnDetailFragment_whenIssueExists() {
        var now = LocalDateTime.now();
        var issue = IssueResponse.builder()
                .id(1L)
                .title("Fix broken pipe")
                .description("The pipe on floor 3 is leaking badly.")
                .status(Status.PREPARED)
                .priority(Priority.HIGH)
                .assignee("Jan Kowalski")
                .sent(false)
                .creatorId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(issueService.getIssueById(1L)).thenReturn(issue);

        var model = new ExtendedModelMap();
        var viewName = fragmentController.getIssueDetailFragment(1L, model);

        assertThat(viewName).isEqualTo("fragments/issue-detail :: issue-detail");
        assertThat(model.getAttribute("issue")).isEqualTo(issue);
    }

    @Test
    void getIssueDetailFragment_shouldReturnNotFoundFragment_whenIssueDoesNotExist() {
        when(issueService.getIssueById(999L)).thenReturn(null);

        var model = new ExtendedModelMap();
        var viewName = fragmentController.getIssueDetailFragment(999L, model);

        assertThat(viewName).isEqualTo("fragments/issue-detail :: issue-not-found");
        assertThat(model.getAttribute("issue")).isNull();
    }

    @Test
    void getIssueDetailFragment_shouldPopulateModelWithIssue_whenIssueHasNullAssignee() {
        var now = LocalDateTime.now();
        var issue = IssueResponse.builder()
                .id(2L)
                .title("Elevator out of order")
                .description("The elevator does not respond.")
                .status(Status.IN_PROGRESS)
                .priority(Priority.CRITICAL)
                .assignee(null)
                .sent(true)
                .creatorId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(issueService.getIssueById(2L)).thenReturn(issue);

        var model = new ExtendedModelMap();
        var viewName = fragmentController.getIssueDetailFragment(2L, model);

        assertThat(viewName).isEqualTo("fragments/issue-detail :: issue-detail");
        var modelIssue = (IssueResponse) model.getAttribute("issue");
        assertThat(modelIssue).isNotNull();
        assertThat(modelIssue.assignee()).isNull();
        assertThat(modelIssue.sent()).isTrue();
    }
}
