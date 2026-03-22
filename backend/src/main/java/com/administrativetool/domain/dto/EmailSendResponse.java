package com.administrativetool.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmailSendResponse {
    private Long issueId;
    private String status;
    private String message;
    private LocalDateTime sentAt;
}
