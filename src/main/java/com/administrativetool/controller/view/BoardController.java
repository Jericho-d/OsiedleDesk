package com.administrativetool.controller.view;

import com.administrativetool.domain.dto.IssueCreateRequest;
import com.administrativetool.service.IssueService;
import com.administrativetool.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
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

@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final IssueService issueService;
    private final UserService userService;

    @GetMapping("/board")
    public String board(final Model model) {
        final var issues = issueService.getAllIssues();

        model.addAttribute("issues", issues);
        return "board/index";
    }

    @GetMapping("/issues/new")
    public String newIssueForm(final Model model) {
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
        if (result.hasErrors()) {
            log.error("Validation errors: {}", result.getAllErrors());
            model.addAttribute("validationErrors", result.getAllErrors());
            return "issues/form";
        }

        final var username = authentication.getName();
        final var user = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        final var creatorId = user.getId();

        issueService.createIssue(request, creatorId, files);

        return "redirect:/board";
    }
}
