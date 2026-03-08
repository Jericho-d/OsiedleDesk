package com.administrativetool.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IssueTest {

    @Test
    void builder_shouldCreateIssueWithAllFields() {
        var expected = Issue.builder()
                .id(1L)
                .title("Test Issue")
                .description("Test Description")
                .status(Status.PREPARED)
                .assignee("user1")
                .sent(true)
                .build();

        var issue = Issue.builder()
                .id(1L)
                .title("Test Issue")
                .description("Test Description")
                .status(Status.PREPARED)
                .assignee("user1")
                .sent(true)
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
                .assignee("user2")
                .sent(false)
                .build();

        var issue = Issue.builder()
                .id(2L)
                .title("Issue Title")
                .description("Issue Description")
                .status(Status.IN_PROGRESS)
                .assignee("user2")
                .sent(false)
                .build();

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
                .status(Status.RESOLVED)
                .assignee("user3")
                .sent(false)
                .build();

        var issue = Issue.builder()
                .id(null)
                .title("Null ID Issue")
                .description("Description")
                .status(Status.RESOLVED)
                .assignee("user3")
                .sent(false)
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
