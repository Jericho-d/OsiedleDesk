package com.administrativetool.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueTest {

    @Test
    void builder_shouldCreateIssueWithAllFields() {
        var expected = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Test Description")
            .status(Status.OPEN)
            .priority(Priority.HIGH)
            .assignee("user1")
            .send(true)
            .build();

        var issue = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Test Description")
            .status(Status.OPEN)
            .priority(Priority.HIGH)
            .assignee("user1")
            .send(true)
            .build();

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void noArgsConstructor_shouldCreateIssueWithNullFields() {
        var expected = Issue.builder().build();

        var issue = new Issue();

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void allArgsConstructor_shouldCreateIssueWithAllFields() {
        var expected = Issue.builder()
            .id(2L)
            .title("Issue Title")
            .description("Issue Description")
            .status(Status.IN_PROGRESS)
            .priority(Priority.MEDIUM)
            .assignee("user2")
            .send(false)
            .build();

        var issue = new Issue(
            2L,
            "Issue Title",
            "Issue Description",
            Status.IN_PROGRESS,
            Priority.MEDIUM,
            "user2",
            false
        );

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void builder_shouldCreateIssueWithMinimalFields() {
        var expected = Issue.builder()
            .title("Minimal Issue")
            .description("Minimal Description")
            .build();

        var issue = Issue.builder()
            .title("Minimal Issue")
            .description("Minimal Description")
            .build();

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void builder_shouldCreateIssueWithNullId() {
        var expected = Issue.builder()
            .id(null)
            .title("Null ID Issue")
            .description("Description")
            .status(Status.DONE)
            .priority(Priority.LOW)
            .assignee("user3")
            .send(false)
            .build();

        var issue = Issue.builder()
            .id(null)
            .title("Null ID Issue")
            .description("Description")
            .status(Status.DONE)
            .priority(Priority.LOW)
            .assignee("user3")
            .send(false)
            .build();

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void builder_shouldCreateIssueWithCriticalPriority() {
        var expected = Issue.builder()
            .id(100L)
            .title("Critical Issue")
            .description("Urgent problem")
            .status(Status.OPEN)
            .priority(Priority.CRITICAL)
            .assignee("admin")
            .send(true)
            .build();

        var issue = Issue.builder()
            .id(100L)
            .title("Critical Issue")
            .description("Urgent problem")
            .status(Status.OPEN)
            .priority(Priority.CRITICAL)
            .assignee("admin")
            .send(true)
            .build();

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void builder_shouldHandleEmptyStrings() {
        var expected = Issue.builder()
            .title("")
            .description("")
            .assignee("")
            .build();

        var issue = Issue.builder()
            .title("")
            .description("")
            .assignee("")
            .build();

        assertThat(issue).usingRecursiveComparison().isEqualTo(expected);
    }
}
