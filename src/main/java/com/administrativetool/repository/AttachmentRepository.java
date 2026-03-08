package com.administrativetool.repository;

import com.administrativetool.domain.model.Attachment;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public interface AttachmentRepository extends ListCrudRepository<Attachment, Long> {

    List<Attachment> findByIssueId(Long issueId);
}
