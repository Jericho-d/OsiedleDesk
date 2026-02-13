package com.administrativetool.controller;

import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final IssueService issueService;

    @GetMapping("/board")
    public String board(Model model) {
        List<IssueResponse> issues = issueService.getAllIssues();
        model.addAttribute("issues", issues);
        return "board/index";
    }

    @GetMapping("/issues/new")
    public String newIssueForm(Model model) {
        model.addAttribute("issue", IssueCreateRequest.builder());
        return "issues/form";
    }

    @PostMapping("/api/issues")
    @ResponseBody
    public ResponseEntity<?> createIssue(
            @Valid @RequestBody IssueCreateRequest request,
            Authentication authentication
    ) {
        final var username = authentication.getName();
        // TODO: Get actual user ID from authentication
        // For now, we'll use a placeholder
        final var creatorId = 1L; // This should come from the authenticated user
        final var issue = issueService.createIssue(request, creatorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    @PostMapping("/issues")
    public String createIssueForm(
            @Valid @ModelAttribute("issue") IssueCreateRequest request,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {
        if (result.hasErrors()) {
            return "issues/form";
        }

        String username = authentication.getName();
        // TODO: Get actual user ID from authentication
        Long creatorId = 1L;

        issueService.createIssue(request, creatorId);
        return "redirect:/board";
    }
}
