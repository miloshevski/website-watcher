package com.websitewatcher.repository;

import com.websitewatcher.entity.WatchedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WatchedUrlRepository extends JpaRepository<WatchedUrl, UUID> {
    List<WatchedUrl> findByUserId(UUID userId);

    @Query(value = """
        SELECT * FROM watched_urls
        WHERE active = true
        AND (
            last_checked_at IS NULL
            OR last_checked_at <= NOW() - (check_interval_minutes * INTERVAL '1 minute')
        )
    """, nativeQuery = true)
    List<WatchedUrl> findDue();
}
