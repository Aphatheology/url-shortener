package com.aphatheology.urlshortener.domain.models;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.aphatheology.urlshortener.domain.entities.ShortUrl}
 */
public record ShortUrlDto(Long id, String shortKey, String originalUrl, UserDto createdBy, Boolean isPrivate,
                          Instant createdAt, Instant expiresAt, Long clickCount) implements Serializable {
}