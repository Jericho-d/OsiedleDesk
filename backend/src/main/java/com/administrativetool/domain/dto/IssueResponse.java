package com.administrativetool.domain.dto;

import com.administrativetool.domain.model.Status;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record IssueResponse(
    Long id,
    String title,
    String description,
    Status status,
    String assignee,
    boolean sent,
    Long creatorId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<AttachmentResponse> attachments
) {
}
