package com.administrativetool.exception;

import lombok.Getter;

public class ResourceNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "%s not found with id: %d";

    @Getter
    private final String resourceName;
    @Getter
    private final Long resourceId;

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
}