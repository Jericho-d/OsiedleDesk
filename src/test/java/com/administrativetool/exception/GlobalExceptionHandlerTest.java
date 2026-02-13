package com.administrativetool.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_shouldReturnNotFoundResponse() {
        var exception = new ResourceNotFoundException("Issue", 1L);
        var request = mock(WebRequest.class);

        var response = exceptionHandler.handleResourceNotFoundException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message", "resource", "id");
        assertThat(body).usingRecursiveComparison().ignoringFields("timestamp").isEqualTo(java.util.Map.of(
            "status", 404,
            "error", "Not Found",
            "message", "Issue not found with id: 1",
            "resource", "Issue",
            "id", 1L
        ));
    }

    @Test
    void handleGenericException_shouldReturnInternalServerErrorResponse() {
        var exception = new RuntimeException("Something went wrong");
        var request = mock(WebRequest.class);

        var response = exceptionHandler.handleGenericException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message");
        assertThat(body).usingRecursiveComparison().ignoringFields("timestamp").isEqualTo(java.util.Map.of(
            "status", 500,
            "error", "Internal Server Error",
            "message", "Something went wrong"
        ));
    }

    @Test
    void handleGenericException_shouldHandleNullPointerException() {
        var exception = new NullPointerException("Null pointer occurred");
        var request = mock(WebRequest.class);

        var response = exceptionHandler.handleGenericException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("timestamp").isEqualTo(java.util.Map.of(
            "status", 500,
            "error", "Internal Server Error",
            "message", "Null pointer occurred"
        ));
    }
}