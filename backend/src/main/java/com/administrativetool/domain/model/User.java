package com.administrativetool.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;

    private String username;

    private String password;

    @Column("role_name")
    private String role;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column("account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column("last_failed_login")
    private LocalDateTime lastFailedLogin;
}
