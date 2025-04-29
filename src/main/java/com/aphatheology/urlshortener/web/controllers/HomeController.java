package com.aphatheology.urlshortener.web.controllers;

import com.aphatheology.urlshortener.ApplicationProperties;
import com.aphatheology.urlshortener.domain.entities.User;
import com.aphatheology.urlshortener.domain.exceptions.ShortUrlNotFoundException;
import com.aphatheology.urlshortener.domain.models.CreateShortUrlCmd;
import com.aphatheology.urlshortener.domain.models.ShortUrlDto;
import com.aphatheology.urlshortener.domain.services.ShortUrlService;
import com.aphatheology.urlshortener.web.dtos.CreateShortUrlForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String home(Model model) {
        List<ShortUrlDto> shortUrls = shortUrlService.getPublicShortUrls();
        User user = securityUtils.getCurrentUser();
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("user", user);
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm(""));
        return "index";
    }

    @PostMapping("/short-urls")
    String createShortUrl(@ModelAttribute("createShortUrlForm") @Valid CreateShortUrlForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            List<ShortUrlDto> shortUrls = shortUrlService.getPublicShortUrls();
            model.addAttribute("shortUrls", shortUrls);
            model.addAttribute("baseUrl", properties.baseUrl());
            return "index";
        }

        try {
            CreateShortUrlCmd cmd = new CreateShortUrlCmd(form.originalUrl());
            shortUrlService.createShortUrl(cmd);
            redirectAttributes.addFlashAttribute("successMessage", "Short URL created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating Short URL!");
        }

        return "redirect:/";
    }

    @GetMapping("s/{shortKey}")
    public String redirectToOriginalUrl(@PathVariable String shortKey) {
        Optional<ShortUrlDto> shortUrlDtoOptional = shortUrlService.getShortUrl(shortKey);
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

}
