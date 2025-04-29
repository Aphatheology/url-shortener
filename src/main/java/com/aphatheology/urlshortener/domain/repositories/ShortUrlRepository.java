package com.aphatheology.urlshortener.domain.repositories;

import com.aphatheology.urlshortener.domain.entities.ShortUrl;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    List<ShortUrl> findByIsPrivateIsFalseOrderByCreatedAtDesc();

//    @Query(nativeQuery = true, value = "SELECT * FROM short_urls WHERE is_private = false ORDER BY created_at DESC")
//    List<ShortUrl> findPublicShortUrls();

//    @Query("SELECT s FROM ShortUrl s LEFT JOIN FETCH s.createdBy WHERE s.isPrivate = false ORDER BY s.createdAt DESC")
    @Query("SELECT s FROM ShortUrl s WHERE s.isPrivate = false ORDER BY s.createdAt DESC")
    @EntityGraph(attributePaths = {"createdBy"})
    List<ShortUrl> findPublicShortUrls();

    boolean existsByShortKey(String shortKey);

    Optional<ShortUrl> findByShortKey(String shortKey);
}