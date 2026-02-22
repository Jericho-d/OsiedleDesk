package com.administrativetool.application.query;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.exception.ResourceNotFoundException;
import com.administrativetool.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IssueQueryService {
    private final IssueRepository repository;

    public List<Issue> findAll() {
        return repository.findAll();
    }

    public Issue findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Issue", id));
    }
}