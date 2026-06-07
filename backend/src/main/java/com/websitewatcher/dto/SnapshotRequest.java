package com.websitewatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SnapshotRequest(
        @NotNull UUID watchedUrlId,
        @NotBlank String contentHash,
        @NotBlank String content
) {}
