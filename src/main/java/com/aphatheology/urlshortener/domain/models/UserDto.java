package com.aphatheology.urlshortener.domain.models;

import java.io.Serializable;

/**
 * DTO for {@link com.aphatheology.urlshortener.domain.entities.User}
 */
public record UserDto(Long id, String name) implements Serializable {
}