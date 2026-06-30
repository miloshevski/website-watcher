package com.websitewatcher.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "watched_urls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchedUrl {

    @Id
    private String id;

    private String userId;

    private String url;

    private String label;

    private String selector;

    private Integer checkIntervalMinutes = 60;

    private Boolean active = true;

    private Instant createdAt = Instant.now();

    private Instant lastCheckedAt;
}
