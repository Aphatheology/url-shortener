package com.aphatheology.urlshortener.web.controllers;

import com.aphatheology.urlshortener.ApplicationProperties;
import com.aphatheology.urlshortener.domain.entities.User;
import com.aphatheology.urlshortener.domain.models.PagedResult;
import com.aphatheology.urlshortener.domain.models.ShortUrlDto;
import com.aphatheology.urlshortener.domain.services.ShortUrlService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ShortUrlService shortUrlService;
    private final ApplicationProperties properties;
    private final SecurityUtils securityUtils;

    public AdminController(ShortUrlService shortUrlService, ApplicationProperties properties, SecurityUtils securityUtils) {
        this.shortUrlService = shortUrlService;
        this.properties = properties;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        User user = securityUtils.getCurrentUser();
        PagedResult<ShortUrlDto> allUrls = shortUrlService.findAllShortUrls(page, size);
        model.addAttribute("shortUrls", allUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("user", user);
        model.addAttribute("paginationUrl", "/admin/dashboard");
        return "admin-dashboard";
    }
}
