package com.websitewatcher.service;

import com.websitewatcher.dto.WatchedUrlRequest;
import com.websitewatcher.dto.WatchedUrlResponse;
import com.websitewatcher.entity.User;
import com.websitewatcher.entity.WatchedUrl;
import com.websitewatcher.repository.NotificationRepository;
import com.websitewatcher.repository.SnapshotRepository;
import com.websitewatcher.repository.UserRepository;
import com.websitewatcher.repository.WatchedUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchedUrlService {

    private final WatchedUrlRepository watchedUrlRepository;
    private final UserRepository userRepository;
    private final SnapshotRepository snapshotRepository;
    private final NotificationRepository notificationRepository;

    public List<WatchedUrlResponse> getForUser(String email) {
        User user = findUser(email);
        return watchedUrlRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WatchedUrlResponse create(String email, WatchedUrlRequest request) {
        User user = findUser(email);
        WatchedUrl entity = new WatchedUrl();
        entity.setUserId(user.getId());
        entity.setUrl(request.url());
        entity.setLabel(request.label());
        entity.setSelector(request.selector());
        entity.setCheckIntervalMinutes(request.checkIntervalMinutes() != null ? request.checkIntervalMinutes() : 60);
        entity.setActive(true);
        return toResponse(watchedUrlRepository.save(entity));
    }

    public WatchedUrlResponse update(String email, String id, WatchedUrlRequest request) {
        WatchedUrl entity = findOwned(email, id);
        entity.setUrl(request.url());
        entity.setLabel(request.label());
        entity.setSelector(request.selector());
        if (request.checkIntervalMinutes() != null) entity.setCheckIntervalMinutes(request.checkIntervalMinutes());
        return toResponse(watchedUrlRepository.save(entity));
    }

    public void delete(String email, String id) {
        WatchedUrl entity = findOwned(email, id);
        snapshotRepository.deleteByWatchedUrlId(id);
        notificationRepository.deleteByWatchedUrlId(id);
        watchedUrlRepository.delete(entity);
    }

    private WatchedUrl findOwned(String email, String id) {
        User user = findUser(email);
        WatchedUrl entity = watchedUrlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WatchedUrl not found"));
        if (!entity.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        return entity;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private WatchedUrlResponse toResponse(WatchedUrl w) {
        return new WatchedUrlResponse(
                w.getId(), w.getUrl(), w.getLabel(), w.getSelector(),
                w.getCheckIntervalMinutes(), w.getActive(),
                w.getCreatedAt(), w.getLastCheckedAt()
        );
    }
}
