package com.aphatheology.urlshortener.web;

import com.aphatheology.urlshortener.domain.exceptions.ShortUrlNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public String handleShortUrlNotFound(ShortUrlNotFoundException ex) {
        log.error("Short URL not found: {}", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex) {
        log.error("An error occurred: {}", ex.getMessage());
        return "error/500";
    }
}
