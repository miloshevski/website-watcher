package com.websitewatcher.dto;

import java.time.Instant;

public record WatchedUrlResponse(
        String id,
        String url,
        String label,
        String selector,
        Integer checkIntervalMinutes,
        Boolean active,
        Instant createdAt,
        Instant lastCheckedAt
) {}
