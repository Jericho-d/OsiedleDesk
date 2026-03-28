package com.administrativetool.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    @Test
    void enumValues_shouldHaveCorrectNames() {
        assertThat(Status.PREPARED.name()).isEqualTo("PREPARED");
        assertThat(Status.IN_PROGRESS.name()).isEqualTo("IN_PROGRESS");
        assertThat(Status.ACKNOWLEDGED.name()).isEqualTo("ACKNOWLEDGED");
        assertThat(Status.RESOLVED.name()).isEqualTo("RESOLVED");
        assertThat(Status.WONT_DO.name()).isEqualTo("WONT_DO");
    }

    @Test
    void enumValues_shouldBeDistinct() {
        var values = Status.values();
        assertThat(values).hasSize(5);
        assertThat(values).containsExactlyInAnyOrder(Status.PREPARED, Status.IN_PROGRESS, Status.ACKNOWLEDGED, Status.RESOLVED, Status.WONT_DO);
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertThat(Status.valueOf("PREPARED")).isEqualTo(Status.PREPARED);
        assertThat(Status.valueOf("IN_PROGRESS")).isEqualTo(Status.IN_PROGRESS);
        assertThat(Status.valueOf("ACKNOWLEDGED")).isEqualTo(Status.ACKNOWLEDGED);
        assertThat(Status.valueOf("RESOLVED")).isEqualTo(Status.RESOLVED);
        assertThat(Status.valueOf("WONT_DO")).isEqualTo(Status.WONT_DO);
    }

    @Test
    void enumOrder_shouldMaintainDeclarationOrder() {
        var values = Status.values();
        assertThat(values).containsExactly(Status.PREPARED, Status.IN_PROGRESS, Status.ACKNOWLEDGED, Status.RESOLVED, Status.WONT_DO);
    }
}
