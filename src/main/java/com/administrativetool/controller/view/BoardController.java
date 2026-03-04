package com.administrativetool.controller.view;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
            final @Valid @ModelAttribute("issue") IssueCreateRequest request,
            final BindingResult result,
            final @RequestParam(value = "files", required = false) List<MultipartFile> files,
            final Authentication authentication,
            final Model model
    ) {
        log.info("createIssueForm called - Title: {}, Description: {}, Priority: {}, Files: {}",
                 request.getTitle(), request.getDescription(), request.getPriority(),
                 files != null ? files.size() : 0);
        
        if (result.hasErrors()) {
            log.error("Validation errors: {}", result.getAllErrors());
            model.addAttribute("validationErrors", result.getAllErrors());
            return "issues/form";
        }

        final var username = authentication.getName();
        log.info("Creating issue for user: {}", username);
        
        final var user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        final var creatorId = user.getId();

        log.info("User found with ID: {}", creatorId);
        
        issueService.createIssue(request, creatorId, files);
        
        log.info("Issue created successfully, redirecting to board");
        return "redirect:/board";
    }
}
