package com.administrativetool.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private LocalDateTime createdAt;
}
