package com.aphatheology.urlshortener.domain.services;

import com.aphatheology.urlshortener.ApplicationProperties;
import com.aphatheology.urlshortener.domain.entities.ShortUrl;
import com.aphatheology.urlshortener.domain.models.CreateShortUrlCmd;
import com.aphatheology.urlshortener.domain.models.ShortUrlDto;
import com.aphatheology.urlshortener.domain.repositories.ShortUrlRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties properties;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, EntityMapper entityMapper, ApplicationProperties properties) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.properties = properties;
    }

    public List<ShortUrlDto> getPublicShortUrls() {
        return shortUrlRepository.findPublicShortUrls().stream().map(entityMapper::toDto).toList();
    }

    public ShortUrlDto createShortUrl(CreateShortUrlCmd cmd) {
        if(properties.validateOriginalUrl()) {
            boolean validUrl = UrlValidator.isUrlExists(cmd.originalUrl());

            if(!validUrl) {
                throw new RuntimeException("Invalid URL: " + cmd.originalUrl());
            }
        }

        var shortKey = generateUniqueShortKey();
        var shortUrl = new ShortUrl();
        shortUrl.setShortKey(shortKey);
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setPrivate(false);
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        shortUrl.setExpiresAt(Instant.now().plus(properties.expiryInDays(), java.time.temporal.ChronoUnit.DAYS));
        shortUrlRepository.save(shortUrl);

        return entityMapper.toDto(shortUrl);
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do{
            shortKey = generateRandomShortKey();
        } while(shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String generateRandomShortKey() {
        StringBuilder shortKey = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            shortKey.append(CHARACTERS.charAt(index));
        }
        return shortKey.toString();
    }
}
