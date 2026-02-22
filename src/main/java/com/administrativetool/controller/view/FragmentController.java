package com.administrativetool.controller.view;

import com.administrativetool.domain.model.Status;
import com.administrativetool.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FragmentController {

    private final IssueService issueService;

    @GetMapping("/fragments/board/column")
    public String getColumnFragment(@RequestParam Status status, Model model) {
        log.debug("Getting column fragment for status: {}", status);
        var issues = issueService.getIssuesByStatus(status);
        log.debug("Found {} issues for status {}", issues.size(), status);
        model.addAttribute("issues", issues);
        model.addAttribute("status", status);
        return "fragments/issue-card :: column-content";
    }

    @GetMapping("/fragments/issues/{id}/card")
    public String getIssueCardFragment(@PathVariable Long id, Model model) {
        var issue = issueService.getIssueById(id);
        model.addAttribute("issue", issue);
        return "fragments/issue-card :: issue-card";
    }
}
