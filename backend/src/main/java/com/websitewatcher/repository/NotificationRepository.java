package com.websitewatcher.repository;

import com.websitewatcher.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndSeen(String userId, Boolean seen);
    void deleteByWatchedUrlId(String watchedUrlId);
}
