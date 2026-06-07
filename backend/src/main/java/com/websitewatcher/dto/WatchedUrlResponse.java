package com.websitewatcher.dto;

import java.time.Instant;
import java.util.UUID;

public record WatchedUrlResponse(
        UUID id,
        String url,
        String label,
        String selector,
        Integer checkIntervalMinutes,
        Boolean active,
        Instant createdAt,
        Instant lastCheckedAt
) {}
