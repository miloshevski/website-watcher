package com.websitewatcher.service;

import com.websitewatcher.dto.WatchedUrlRequest;
import com.websitewatcher.dto.WatchedUrlResponse;
import com.websitewatcher.entity.User;
import com.websitewatcher.entity.WatchedUrl;
import com.websitewatcher.repository.UserRepository;
import com.websitewatcher.repository.WatchedUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchedUrlService {

    private final WatchedUrlRepository watchedUrlRepository;
    private final UserRepository userRepository;

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
        entity.setUser(user);
        entity.setUrl(request.url());
        entity.setLabel(request.label());
        entity.setSelector(request.selector());
        entity.setCheckIntervalMinutes(request.checkIntervalMinutes() != null ? request.checkIntervalMinutes() : 60);
        entity.setActive(true);
        return toResponse(watchedUrlRepository.save(entity));
    }

    public WatchedUrlResponse update(String email, UUID id, WatchedUrlRequest request) {
        WatchedUrl entity = findOwned(email, id);
        if (request.label() != null) entity.setLabel(request.label());
        if (request.selector() != null) entity.setSelector(request.selector());
        if (request.checkIntervalMinutes() != null) entity.setCheckIntervalMinutes(request.checkIntervalMinutes());
        return toResponse(watchedUrlRepository.save(entity));
    }

    public void delete(String email, UUID id) {
        WatchedUrl entity = findOwned(email, id);
        watchedUrlRepository.delete(entity);
    }

    private WatchedUrl findOwned(String email, UUID id) {
        User user = findUser(email);
        WatchedUrl entity = watchedUrlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WatchedUrl not found"));
        if (!entity.getUser().getId().equals(user.getId())) {
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
