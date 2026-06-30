package com.websitewatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SnapshotRequest(
        @NotNull String watchedUrlId,
        @NotBlank String contentHash,
        @NotBlank String content
) {}
