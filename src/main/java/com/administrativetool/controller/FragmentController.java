package com.administrativetool.controller;

import com.administrativetool.domain.model.Status;
import com.administrativetool.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class FragmentController {

    private final IssueService issueService;

    @GetMapping("/fragments/board/column")
    public String getColumnFragment(@RequestParam Status status, Model model) {
        var issues = issueService.getIssuesByStatus(status);
        model.addAttribute("issues", issues);
        model.addAttribute("status", status);
        return "fragments/column :: column-content";
    }

    @GetMapping("/fragments/issues/{id}/card")
    public String getIssueCardFragment(@PathVariable Long id, Model model) {
        var issue = issueService.getIssueById(id);
        model.addAttribute("issue", issue);
        return "fragments/issue-card :: issue-card";
    }
}
