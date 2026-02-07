package com.administrativetool.domain.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueDeletedEventTest {

    @Test
    void constructor_shouldCreateEventWithSourceAndIssueId() {
        var source = new Object();
        var issueId = 1L;

        var event = new IssueDeletedEvent(source, issueId);

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().ignoringFields("source").isEqualTo(
            IssueDeletedEvent.builder().source(source).issueId(issueId).build()
        );
    }

    @Test
    void builder_shouldCreateEventWithSourceAndIssueId() {
        var source = new Object();
        var issueId = 2L;

        var event = IssueDeletedEvent.builder()
            .source(source)
            .issueId(issueId)
            .build();

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().ignoringFields("source").isEqualTo(
            IssueDeletedEvent.builder().source(source).issueId(issueId).build()
        );
    }

    @Test
    void noArgsConstructor_shouldCreateEventWithNullFields() {
        var event = new IssueDeletedEvent();

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().isEqualTo(IssueDeletedEvent.builder().build());
    }

    @Test
    void constructor_shouldCreateEventWithLargeIssueId() {
        var source = new Object();
        var issueId = 999999L;

        var event = new IssueDeletedEvent(source, issueId);

        assertThat(event).isNotNull();
        assertThat(event).usingRecursiveComparison().ignoringFields("source").isEqualTo(
            IssueDeletedEvent.builder().source(source).issueId(issueId).build()
        );
    }
}
