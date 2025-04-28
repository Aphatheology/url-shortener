package com.aphatheology.urlshortener.services;

import com.aphatheology.urlshortener.domain.entities.ShortUrl;
import com.aphatheology.urlshortener.domain.entities.User;
import com.aphatheology.urlshortener.domain.models.ShortUrlDto;
import com.aphatheology.urlshortener.domain.models.UserDto;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public ShortUrlDto toDto(ShortUrl shortUrl) {
        UserDto userDto = null;
        if (shortUrl.getCreatedBy() != null) {
            userDto = new UserDto(
                    shortUrl.getCreatedBy().getId(),
                    shortUrl.getCreatedBy().getName()
            );
        }
        return new ShortUrlDto(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortKey(),
                userDto,
                shortUrl.getPrivate(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresAt(),
                shortUrl.getClickCount()
        );
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName()
        );
    }
}
