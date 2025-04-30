package com.aphatheology.urlshortener.web.controllers;

import com.aphatheology.urlshortener.ApplicationProperties;
import com.aphatheology.urlshortener.domain.entities.User;
import com.aphatheology.urlshortener.domain.exceptions.ShortUrlNotFoundException;
import com.aphatheology.urlshortener.domain.models.CreateShortUrlCmd;
import com.aphatheology.urlshortener.domain.models.PagedResult;
import com.aphatheology.urlshortener.domain.models.ShortUrlDto;
import com.aphatheology.urlshortener.domain.services.ShortUrlService;
import com.aphatheology.urlshortener.web.dtos.CreateShortUrlForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties properties;
    private final SecurityUtils securityUtils;

    public HomeController(ShortUrlService shortUrlService, ApplicationProperties properties, SecurityUtils securityUtils) {
        this.shortUrlService = shortUrlService;
        this.properties = properties;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
//            @PageableDefault(page = 1, size = 10)
//            Pageable pageable,
            Model model) {
        PagedResult<ShortUrlDto> shortUrls = shortUrlService.getPublicShortUrls(page, size);
        User user = securityUtils.getCurrentUser();
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("paginationUrl", "/");
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("user", user);
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm("", false, null));
        return "index";
    }

    @PostMapping("/short-urls")
    String createShortUrl(@ModelAttribute("createShortUrlForm") @Valid CreateShortUrlForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            PagedResult<ShortUrlDto> shortUrls = shortUrlService.getPublicShortUrls(1, 10);
            model.addAttribute("shortUrls", shortUrls);
            model.addAttribute("paginationUrl", "/");
            model.addAttribute("baseUrl", properties.baseUrl());
            return "index";
        }

        try {
            Long userId = securityUtils.getCurrentUserId();
            CreateShortUrlCmd cmd = new CreateShortUrlCmd(
                    form.originalUrl(),
                    form.isPrivate(),
                    form.expirationInDays(),
                    userId
            );
            shortUrlService.createShortUrl(cmd);
            redirectAttributes.addFlashAttribute("successMessage", "Short URL created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating Short URL!");
        }

        return "redirect:/";
    }

    @GetMapping("s/{shortKey}")
    public String redirectToOriginalUrl(@PathVariable String shortKey) {
        Long userId = securityUtils.getCurrentUserId();
        Optional<ShortUrlDto> shortUrlDtoOptional = shortUrlService.getShortUrl(shortKey, userId);
        if (shortUrlDtoOptional.isEmpty()) {
            throw new ShortUrlNotFoundException("Short URL not found");
        }
        ShortUrlDto shortUrl = shortUrlDtoOptional.get();
        return "redirect:" + shortUrl.originalUrl();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/my-urls")
    public String showUserUrls(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        var currentUserId = securityUtils.getCurrentUserId();
        User user = securityUtils.getCurrentUser();
        PagedResult<ShortUrlDto> myUrls =
                shortUrlService.getUserShortUrls(currentUserId, page, size);
        model.addAttribute("shortUrls", myUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("user", user);
        model.addAttribute("paginationUrl", "/my-urls");
        return "my-urls";
    }

    @PostMapping("/delete-urls")
    public String deleteUrls(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "No URLs selected for deletion");
            return "redirect:/my-urls";
        }
        try {
            var currentUserId = securityUtils.getCurrentUserId();
            shortUrlService.deleteUserShortUrls(ids, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Selected URLs have been deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting URLs: " + e.getMessage());
        }
        return "redirect:/my-urls";
    }

}
