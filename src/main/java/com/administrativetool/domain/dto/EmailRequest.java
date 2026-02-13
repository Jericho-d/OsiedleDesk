package com.administrativetool.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    
    @NotNull(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;
    
    @NotNull(message = "Subject is required")
    private String subject;
    
    @NotNull(message = "Body is required")
    private String body;
}
