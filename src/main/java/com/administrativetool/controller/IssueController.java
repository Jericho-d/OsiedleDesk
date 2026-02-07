package com.administrativetool.controller;

import com.administrativetool.application.command.IssueCommandService;
import com.administrativetool.application.query.IssueQueryService;
import com.administrativetool.domain.model.Issue;
import com.administrativetool.domain.model.Status;
import com.administrativetool.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IssueController {

    private final IssueQueryService queryService;
    private final IssueCommandService commandService;
    private final EmailService emailService;

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
    public ResponseEntity<Void> sendIssue(@PathVariable Long id) {
        Issue issue = queryService.findById(id);
        if (issue == null) {
            return ResponseEntity.notFound().build();
        }
        if (issue.isSent()) {
            return ResponseEntity.ok().build();
        }
        emailService.sendIssueEmail(issue);
        issue.setStatus(Status.IN_PROGRESS);
        issue.setSent(true);
        commandService.update(id, issue);
        return ResponseEntity.ok().build();
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