package com.aphatheology.urlshortener.domain.services;

import com.aphatheology.urlshortener.ApplicationProperties;
import com.aphatheology.urlshortener.domain.entities.ShortUrl;
import com.aphatheology.urlshortener.domain.models.CreateShortUrlCmd;
import com.aphatheology.urlshortener.domain.models.PagedResult;
import com.aphatheology.urlshortener.domain.models.ShortUrlDto;
import com.aphatheology.urlshortener.domain.repositories.ShortUrlRepository;
import com.aphatheology.urlshortener.domain.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.*;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties properties;
    private final UserRepository userRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, EntityMapper entityMapper, ApplicationProperties properties, UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.properties = properties;
        this.userRepository = userRepository;
    }

    public PagedResult<ShortUrlDto> getPublicShortUrls(int page, int size) {
        page = page > 1 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//        return shortUrlRepository.findPublicShortUrls().stream().map(entityMapper::toShortUrlDto).toList();
        Page<ShortUrlDto> shortUrlDtoPage = shortUrlRepository.findPublicShortUrlsPageable(pageable).map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlDtoPage);
    }

    public PagedResult<ShortUrlDto> getUserShortUrls(Long userId, int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage = shortUrlRepository.findByCreatedById(userId, pageable)
                .map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlsPage);
    }

    @Transactional
    public void deleteUserShortUrls(List<Long> ids, Long userId) {
        if (ids != null && !ids.isEmpty() && userId != null) {
            shortUrlRepository.deleteByIdInAndCreatedById(ids, userId);
        }
    }

    public PagedResult<ShortUrlDto> findAllShortUrls(int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage =  shortUrlRepository.findAllShortUrls(pageable).map(entityMapper::toShortUrlDto);
        return PagedResult.from(shortUrlsPage);
    }

    private Pageable getPageable(int page, int size) {
        page = page > 1 ? page - 1: 0;
        return PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
    }

    @Transactional
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
        if(cmd.userId() == null) {
            shortUrl.setCreatedBy(null);
            shortUrl.setPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(properties.expiryInDays(), DAYS));
        } else {
            shortUrl.setCreatedBy(userRepository.findById(cmd.userId()).orElseThrow());
            shortUrl.setPrivate(cmd.isPrivate() != null && cmd.isPrivate());
            shortUrl.setExpiresAt(cmd.expirationInDays() != null ? Instant.now().plus(cmd.expirationInDays(), DAYS) : null);
        }
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        shortUrlRepository.save(shortUrl);

        return entityMapper.toShortUrlDto(shortUrl);
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

    @Transactional
    public Optional<ShortUrlDto> getShortUrl(String shortKey, Long userId) {
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortKey(shortKey);
        if(shortUrlOptional.isEmpty()) return Optional.empty();

        ShortUrl shortUrl = shortUrlOptional.get();
        if(shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }

        if(shortUrl.getPrivate() && shortUrl.getCreatedBy() != null && !shortUrl.getCreatedBy().getId().equals(userId)) {
            return Optional.empty();
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        return shortUrlOptional.map(entityMapper::toShortUrlDto);
    }
}
