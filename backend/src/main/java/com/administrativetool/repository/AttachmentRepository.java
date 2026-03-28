package com.administrativetool.repository;

import com.administrativetool.domain.model.Attachment;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface AttachmentRepository extends ListCrudRepository<Attachment, Long> {

    List<Attachment> findByIssueId(Long issueId);
}
