package com.websitewatcher.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    @Id
    private String id;

    private String watchedUrlId;

    private String contentHash;

    private String content;

    private Instant capturedAt = Instant.now();
}
