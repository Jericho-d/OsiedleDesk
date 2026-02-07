package com.administrativetool.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_shouldCreateExceptionWithResourceNameAndId() {
        var exception = new ResourceNotFoundException("Issue", 1L);

        assertThat(exception).isNotNull();
        assertThat(exception).hasFieldOrPropertyWithValue("resourceName", "Issue");
        assertThat(exception).hasFieldOrPropertyWithValue("resourceId", 1L);
        assertThat(exception).hasMessage("Issue not found with id: 1");
    }

    @Test
    void constructor_shouldCreateExceptionWithDifferentResourceName() {
        var exception = new ResourceNotFoundException("User", 42L);

        assertThat(exception).isNotNull();
        assertThat(exception).hasFieldOrPropertyWithValue("resourceName", "User");
        assertThat(exception).hasFieldOrPropertyWithValue("resourceId", 42L);
        assertThat(exception).hasMessage("User not found with id: 42");
    }

    @Test
    void constructor_shouldCreateExceptionWithZeroId() {
        var exception = new ResourceNotFoundException("TestResource", 0L);

        assertThat(exception).isNotNull();
        assertThat(exception).hasFieldOrPropertyWithValue("resourceId", 0L);
        assertThat(exception).hasMessage("TestResource not found with id: 0");
    }

    @Test
    void throwException_shouldContainCorrectMessage() {
        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException("Issue", 99L);
        })
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Issue not found with id: 99")
            .hasFieldOrPropertyWithValue("resourceName", "Issue")
            .hasFieldOrPropertyWithValue("resourceId", 99L);
    }
}
