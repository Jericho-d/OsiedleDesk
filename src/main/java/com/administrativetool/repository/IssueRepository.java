package com.administrativetool.repository;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends ListCrudRepository<Issue, Long> {

    List<Issue> findAllByOrderByCreatedAtDesc();

    List<Issue> findByStatusOrderByCreatedAtDesc(Status status);

    @Query("SELECT * FROM issues WHERE creator_id = :creatorId ORDER BY created_at DESC")
    List<Issue> findByCreatorId(@Param("creatorId") Long creatorId);

    @Query("SELECT * FROM issues WHERE title ILIKE '%' || :keyword || '%' OR description ILIKE '%' || :keyword || '%'")
    List<Issue> searchByKeyword(@Param("keyword") String keyword);
}
