package com.administrativetool.controller.api;

import com.administrativetool.application.command.IssueCommandService;
import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import com.administrativetool.service.IssueEmailService;
import com.administrativetool.service.IssueService;
import com.administrativetool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueQueryService queryService;
    private final IssueCommandService commandService;
    private final IssueService issueService;
    private final IssueEmailService issueEmailService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IssueResponse> createWithFiles(
        final @Valid @RequestPart("issue") IssueCreateRequest request,
        final @RequestPart(value = "files", required = false) List<MultipartFile> files,
        final Authentication authentication
    ) {
        final var username = authentication.getName();
        final var user = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        final var creatorId = user.getId();
        final var response = issueService.createIssue(request, creatorId, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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
    public List<IssueResponse> list() {
        return issueService.getAllIssues();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> get(final @PathVariable Long id) {
        final var issue = issueService.getIssueById(id);
        if (issue == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
        final @PathVariable Long id,
        final @RequestBody Issue updated
    ) {
        commandService.update(id, updated);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(final @PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> sendIssue(final @PathVariable Long id) {
        try {
            issueEmailService.sendIssueToAdmin(id);
            final var issue = issueService.getIssueById(id);
            return ResponseEntity.ok(Map.of(
                "message", "Issue sent successfully",
                "issue", issue
            ));
        } catch (IllegalArgumentException e) {
            log.error("Failed to send issue {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to send issue {} to admin: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> moveStatus(
        final @PathVariable Long id,
        final @RequestParam String target
    ) {
        try {
            final var newStatus = Status.valueOf(target);
            final var issue = issueService.updateIssueStatus(id, newStatus);
            return ResponseEntity.ok(Map.of(
                "message", "Status updated to " + newStatus,
                "issue", issue
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status move for issue {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to move status for issue {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
