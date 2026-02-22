package com.administrativetool.controller.api;

import com.administrativetool.application.command.IssueCommandService;
import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.service.IssueService;
import com.administrativetool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueQueryService queryService;
    private final IssueCommandService commandService;
    private final IssueService issueService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<IssueResponse> create(
            final @Valid @RequestBody IssueCreateRequest request,
            final Authentication authentication
    ) {
        final var username = authentication.getName();
        final var user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        final var creatorId = user.getId();
        final var response = issueService.createIssue(request, creatorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @ResponseBody
    public List<Issue> list() {
        return queryService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Issue get(@PathVariable Long id) {
        return queryService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Issue updated) {
        commandService.update(id, updated);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.accepted().build();
    }
}