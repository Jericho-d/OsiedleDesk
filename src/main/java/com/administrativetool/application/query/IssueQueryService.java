package com.administrativetool.application.query;

import com.administrativetool.domain.model.Issue;
import com.administrativetool.exception.ResourceNotFoundException;
import com.administrativetool.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IssueQueryService {
    private final IssueRepository repository;

    @Autowired
    public IssueQueryService(IssueRepository repository) {
        this.repository = repository;
    }

    public List<Issue> findAll() {
        List<Issue> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }

    public Issue findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Issue", id));
    }
}