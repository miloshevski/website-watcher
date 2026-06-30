package com.websitewatcher.dto;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String watchedUrlId,
        String message,
        Boolean seen,
        Instant createdAt
) {}
