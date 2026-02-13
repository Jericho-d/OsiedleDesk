package com.administrativetool.controller;

import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.domain.dto.IssueResponse;
import com.administrativetool.service.IssueService;
import com.administrativetool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final IssueService issueService;
    private final UserService userService;

    @GetMapping("/board")
    public String board(Model model) {
        List<IssueResponse> issues = issueService.getAllIssues();
        model.addAttribute("issues", issues);
        return "board/index";
    }

    @GetMapping("/issues/new")
    public String newIssueForm(Model model) {
        model.addAttribute("issue", new IssueCreateRequest());
        return "issues/form";
    }

    @PostMapping("/issues")
    public String createIssueForm(
            @Valid @ModelAttribute("issue") IssueCreateRequest request,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {
        log.info("createIssueForm called - Title: {}, Description: {}, Priority: {}", 
                 request.getTitle(), request.getDescription(), request.getPriority());
        
        if (result.hasErrors()) {
            log.error("Validation errors: {}", result.getAllErrors());
            return "issues/form";
        }

        final var username = authentication.getName();
        log.info("Creating issue for user: {}", username);
        
        final var user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        final var creatorId = user.getId();

        log.info("User found with ID: {}", creatorId);
        
        issueService.createIssue(request, creatorId);
        
        log.info("Issue created successfully, redirecting to board");
        return "redirect:/board";
    }
}
