package com.administrativetool.controller.view;

import com.administrativetool.domain.model.Status;
import com.administrativetool.service.IssueEmailService;
import com.administrativetool.service.IssueService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueViewController {

    private final IssueEmailService issueEmailService;
    private final IssueService issueService;

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String sendIssue(
            final @PathVariable Long id,
            final Model model
    ) {
        try {
            issueEmailService.sendIssueToAdmin(id);
            final var issue = issueService.getIssueById(id);
            model.addAttribute("issue", issue);
            model.addAttribute("oob", true);
            return "fragments/issue-card :: issue-card";
        } catch (Exception e) {
            log.error("Failed to send issue {} to admin: {}", id, e.getMessage(), e);
            model.addAttribute("errorDetail", friendlyMessage(e));
            return "fragments/send-error :: send-error";
        }
    }

    @PostMapping("/{id}/send/detail")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String sendIssueFromDetail(
            final @PathVariable Long id,
            final Model model
    ) {
        try {
            issueEmailService.sendIssueToAdmin(id);
            final var issue = issueService.getIssueById(id);
            model.addAttribute("issue", issue);
            model.addAttribute("oob", true);
            return "fragments/issue-detail :: issue-detail";
        } catch (Exception e) {
            log.error("Failed to send issue {} to admin (detail): {}", id, e.getMessage(), e);
            model.addAttribute("errorDetail", friendlyMessage(e));
            return "fragments/send-error :: send-error";
        }
    }

    private static String friendlyMessage(final Exception e) {
        final var msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return "The email could not be delivered and the issue status was not changed. Check the SMTP configuration and try again.";
        }
        return msg;
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String moveStatus(
            final @PathVariable Long id,
            final @RequestParam String target,
            final @RequestParam(required = false) String source,
            final Model model,
            final HttpServletResponse response
    ) {
        try {
            final var newStatus = Status.valueOf(target);
            issueService.updateIssueStatus(id, newStatus);
            final var issue = issueService.getIssueById(id);
            model.addAttribute("issue", issue);
            model.addAttribute("oob", true);
            if ("detail".equals(source)) {
                return "fragments/issue-detail :: issue-detail";
            }
            return "fragments/issue-card :: issue-card";
        } catch (IllegalArgumentException e) {
            log.error("Invalid status move for issue {}: {}", id, e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("errorDetail", e.getMessage());
            return "fragments/send-error :: send-error";
        } catch (Exception e) {
            log.error("Failed to move status for issue {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorDetail", e.getMessage());
            return "fragments/send-error :: send-error";
        }
    }
}
