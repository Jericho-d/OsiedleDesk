package com.administrativetool.domain.dto;

import lombok.Builder;

@Builder
public record AttachmentResponse(
    Long id,
    Long issueId,
    String filename,
    String contentType,
    Long size
) {
}
