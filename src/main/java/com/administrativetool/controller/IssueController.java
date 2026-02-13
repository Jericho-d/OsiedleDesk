package com.administrativetool.controller;

import com.administrativetool.application.command.IssueCommandService;
import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import com.administrativetool.service.EmailService;
import com.administrativetool.service.IssueEmailService;
import com.administrativetool.service.IssueService;
import com.administrativetool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueQueryService queryService;
    private final IssueCommandService commandService;
    private final EmailService emailService;
    private final IssueEmailService issueEmailService;
    private final IssueService issueService;
    private final UserService userService;

    @PostMapping
    @ResponseBody
    public ResponseEntity<IssueResponse> create(
            @Valid @RequestBody IssueCreateRequest request,
            Authentication authentication
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

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public String sendIssue(@PathVariable Long id, Model model) {
        issueEmailService.sendIssueToAdmin(id);
        
        final var issue = issueService.getIssueById(id);
        model.addAttribute("issue", issue);
        return "fragments/issue-card :: issue-card";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Model model) {
        final var newStatus = Status.valueOf(status);
        final var updatedIssue = issueService.updateIssueStatus(id, newStatus);
        
        model.addAttribute("issue", updatedIssue);
        return "fragments/issue-card :: issue-card";
    }

    @GetMapping(value = "/html", produces = "text/html")
    public String listHtml() {
        StringBuilder sb = new StringBuilder();
        for (Issue i : queryService.findAll()) {
            sb.append("<div class='issue'>");
            sb.append("<strong>").append(i.getTitle()).append("</strong>");
            sb.append(" - ").append(i.getDescription());
            sb.append(" [" ).append(i.getStatus()).append(" / ").append(i.getPriority()).append("]");
            sb.append("</div>");
        }
        return sb.toString();
    }
}
