package com.websitewatcher.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID watchedUrlId,
        String message,
        Boolean seen,
        Instant createdAt
) {}
