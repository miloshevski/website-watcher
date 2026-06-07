package com.websitewatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record WatchedUrlRequest(
        @NotBlank String url,
        @NotBlank String label,
        String selector,
        @Positive Integer checkIntervalMinutes
) {}
