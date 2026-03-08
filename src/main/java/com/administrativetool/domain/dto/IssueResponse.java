package com.administrativetool.domain.dto;

import com.administrativetool.domain.model.Status;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

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
