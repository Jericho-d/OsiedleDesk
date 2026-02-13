package com.administrativetool.controller;

import com.administrativetool.application.command.IssueCommandService;
import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.dto.EmailSendResponse;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.service.EmailService;
import com.administrativetool.service.IssueEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IssueController {

    private final IssueQueryService queryService;
    private final IssueCommandService commandService;
    private final EmailService emailService;
    private final IssueEmailService issueEmailService;

    @GetMapping
    public List<Issue> list() {
        return queryService.findAll();
    }

    @GetMapping("/{id}")
    public Issue get(@PathVariable Long id) {
        return queryService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Issue updated) {
        commandService.update(id, updated);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailSendResponse> sendIssue(@PathVariable Long id) {
        EmailSendResponse response = issueEmailService.sendIssueToAdmin(id);
        
        return switch (response.getStatus()) {
            case "QUEUED" -> ResponseEntity.ok(response);
            case "ALREADY_SENT" -> ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            case "NOT_FOUND" -> ResponseEntity.notFound().build();
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        };
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
