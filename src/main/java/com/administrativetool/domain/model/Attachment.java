package com.administrativetool.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("attachments")
public class Attachment {
    @Id
    private Long id;

    @Column("issue_id")
    private Long issueId;

    private String filename;

    @Column("content_type")
    private String contentType;

    private byte[] data;

    private Long size;

    @Column("created_at")
    private LocalDateTime createdAt;
}
