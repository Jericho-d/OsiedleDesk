package com.administrativetool.domain.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import org.junit.jupiter.api.Test;

class IssueUpdatedEventTest {

    @Test
    void constructor_shouldCreateEventWithSourceAndIssue() {
        var source = new Object();
        var issue = Issue.builder()
                .id(1L)
                .title("Test Issue")
                .description("Description")
                .status(Status.PREPARED)
                .assignee("user1")
                .sent(false)
                .build();

        var event = new IssueUpdatedEvent(source, issue);

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().ignoringFields("source").isEqualTo(
                IssueUpdatedEvent.builder().source(source).issue(issue).build()
        );
    }

    @Test
    void builder_shouldCreateEventWithSourceAndIssue() {
        var source = new Object();
        var issue = Issue.builder()
                .id(2L)
                .title("Updated Issue")
                .description("Updated Description")
                .status(Status.RESOLVED)
                .assignee("user2")
                .sent(true)
                .build();

        var event = IssueUpdatedEvent.builder()
                .source(source)
                .issue(issue)
                .build();

        assertThat(event).isNotNull();
        assertThat(event.getIssue()).usingRecursiveComparison().isEqualTo(issue);
    }

    @Test
    void builder_shouldCreateEventWithNullFields() {
        var event = IssueUpdatedEvent.builder().source(this).build();

        assertThat(event).isNotNull();
        assertThat(event.getIssue()).isNull();
    }
}
