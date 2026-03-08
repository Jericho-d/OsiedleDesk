package com.administrativetool.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSendResponse {
    private Long issueId;
    private String status;
    private String message;
    private LocalDateTime sentAt;
}
