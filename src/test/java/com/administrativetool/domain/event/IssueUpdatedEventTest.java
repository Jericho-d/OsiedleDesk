package com.administrativetool.domain.event;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Priority;
import com.administrativetool.domain.model.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueUpdatedEventTest {

    @Test
    void constructor_shouldCreateEventWithSourceAndIssue() {
        var source = new Object();
        var issue = Issue.builder()
            .id(1L)
            .title("Test Issue")
            .description("Description")
            .status(Status.OPEN)
            .priority(Priority.HIGH)
            .assignee("user1")
            .send(false)
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
            .status(Status.DONE)
            .priority(Priority.CRITICAL)
            .assignee("user2")
            .send(true)
            .build();

        var event = IssueUpdatedEvent.builder()
            .source(source)
            .issue(issue)
            .build();

        assertThat(event).isNotNull();
        assertThat(event.getIssue()).usingRecursiveComparison().isEqualTo(issue);
    }

    @Test
    void noArgsConstructor_shouldCreateEventWithNullFields() {
        var event = new IssueUpdatedEvent();

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().isEqualTo(IssueUpdatedEvent.builder().build());
    }
}
