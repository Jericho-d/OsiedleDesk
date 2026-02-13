package com.administrativetool.domain.event;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueCreatedEventTest {

    @Test
    void constructor_shouldCreateEventWithSourceAndIssue() {
        var source = new Object();
        var issue = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Description")
            .status(Status.PREPARED)
            .priority(Priority.HIGH)
            .assignee("user1")
            .sent(false)
            .build();

        var event = new IssueCreatedEvent(source, issue);

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().ignoringFields("source").isEqualTo(
            IssueCreatedEvent.builder().source(source).issue(issue).build()
        );
    }

    @Test
    void builder_shouldCreateEventWithSourceAndIssue() {
        var source = new Object();
        var issue = Issue.builder()
            .id(2L)
            .title("Builder Issue")
            .description("Builder Description")
            .status(Status.IN_PROGRESS)
            .priority(Priority.MEDIUM)
            .assignee("user2")
            .sent(true)
            .build();

        var event = IssueCreatedEvent.builder()
            .source(source)
            .issue(issue)
            .build();

        assertThat(event).isNotNull();
        assertThat(event.getIssue()).usingRecursiveComparison().isEqualTo(issue);
    }

    @Test
    void builder_shouldCreateEventWithNullFields() {
        var event = IssueCreatedEvent.builder().source(this).build();

        assertThat(event).isNotNull();
        assertThat(event.getIssue()).isNull();
    }
}
