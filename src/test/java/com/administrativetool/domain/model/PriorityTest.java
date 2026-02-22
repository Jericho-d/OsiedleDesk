package com.administrativetool.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityTest {

    @Test
    void enumValues_shouldHaveCorrectNames() {
        assertThat(Priority.LOW.name()).isEqualTo("LOW");
        assertThat(Priority.MEDIUM.name()).isEqualTo("MEDIUM");
        assertThat(Priority.HIGH.name()).isEqualTo("HIGH");
        assertThat(Priority.CRITICAL.name()).isEqualTo("CRITICAL");
    }

    @Test
    void enumValues_shouldBeDistinct() {
        var values = Priority.values();
        assertThat(values).hasSize(4);
        assertThat(values).containsExactlyInAnyOrder(
            Priority.LOW,
            Priority.MEDIUM,
            Priority.HIGH,
            Priority.CRITICAL
        );
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertThat(Priority.valueOf("LOW")).isEqualTo(Priority.LOW);
        assertThat(Priority.valueOf("MEDIUM")).isEqualTo(Priority.MEDIUM);
        assertThat(Priority.valueOf("HIGH")).isEqualTo(Priority.HIGH);
        assertThat(Priority.valueOf("CRITICAL")).isEqualTo(Priority.CRITICAL);
    }

    @Test
    void enumOrder_shouldMaintainDeclarationOrder() {
        var values = Priority.values();
        assertThat(values).containsExactly(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.CRITICAL);
    }
}
