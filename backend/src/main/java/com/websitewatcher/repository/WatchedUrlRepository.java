package com.websitewatcher.repository;

import com.websitewatcher.entity.WatchedUrl;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WatchedUrlRepository extends MongoRepository<WatchedUrl, String> {
    List<WatchedUrl> findByUserId(String userId);
    List<WatchedUrl> findByActive(boolean active);
}
