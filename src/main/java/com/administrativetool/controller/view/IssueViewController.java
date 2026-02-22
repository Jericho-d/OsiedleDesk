package com.administrativetool.controller.view;

import com.administrativetool.domain.model.Status;
import com.administrativetool.service.IssueEmailService;
import com.administrativetool.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueViewController {

    private final IssueEmailService issueEmailService;
    private final IssueService issueService;

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public String sendIssue(
            final @PathVariable Long id,
            final Model model
    ) {
        issueEmailService.sendIssueToAdmin(id);

        final var issue = issueService.getIssueById(id);
        model.addAttribute("issue", issue);
        return "fragments/issue-card :: issue-card";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatus(
            final @PathVariable Long id,
            final @RequestParam String status,
            final Model model
    ) {
        final var newStatus = Status.valueOf(status);
        final var updatedIssue = issueService.updateIssueStatus(id, newStatus);

        model.addAttribute("issue", updatedIssue);
        return "fragments/issue-card :: issue-card";
    }

    @PostMapping("/{id}/send/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public String sendIssueFromDetail(
            final @PathVariable Long id,
            final Model model
    ) {
        issueEmailService.sendIssueToAdmin(id);

        final var issue = issueService.getIssueById(id);
        model.addAttribute("issue", issue);
        return "fragments/issue-detail :: issue-detail";
    }

    @PostMapping("/{id}/status/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatusFromDetail(
            final @PathVariable Long id,
            final @RequestParam String status,
            final Model model
    ) {
        final var newStatus = Status.valueOf(status);
        final var updatedIssue = issueService.updateIssueStatus(id, newStatus);

        model.addAttribute("issue", updatedIssue);
        return "fragments/issue-detail :: issue-detail";
    }
}
