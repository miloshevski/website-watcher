package com.websitewatcher.service;

import com.websitewatcher.dto.SnapshotRequest;
import com.websitewatcher.dto.WatchedUrlResponse;
import com.websitewatcher.entity.Notification;
import com.websitewatcher.entity.Snapshot;
import com.websitewatcher.entity.WatchedUrl;
import com.websitewatcher.repository.NotificationRepository;
import com.websitewatcher.repository.SnapshotRepository;
import com.websitewatcher.repository.WatchedUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalService {

    private final WatchedUrlRepository watchedUrlRepository;
    private final SnapshotRepository snapshotRepository;
    private final NotificationRepository notificationRepository;

    public List<WatchedUrlResponse> getDueUrls() {
        Instant now = Instant.now();
        return watchedUrlRepository.findByActive(true)
                .stream()
                .filter(w -> w.getLastCheckedAt() == null ||
                        w.getLastCheckedAt().plusSeconds(w.getCheckIntervalMinutes() * 60L).isBefore(now))
                .map(w -> new WatchedUrlResponse(
                        w.getId(), w.getUrl(), w.getLabel(), w.getSelector(),
                        w.getCheckIntervalMinutes(), w.getActive(),
                        w.getCreatedAt(), w.getLastCheckedAt()
                ))
                .toList();
    }

    public void submitSnapshot(SnapshotRequest request) {
        WatchedUrl watchedUrl = watchedUrlRepository.findById(request.watchedUrlId())
                .orElseThrow(() -> new IllegalArgumentException("WatchedUrl not found"));

        var latest = snapshotRepository.findTopByWatchedUrlIdOrderByCapturedAtDesc(watchedUrl.getId());
        boolean changed = latest.map(s -> !s.getContentHash().equals(request.contentHash())).orElse(true);

        Snapshot snapshot = new Snapshot();
        snapshot.setWatchedUrlId(watchedUrl.getId());
        snapshot.setContentHash(request.contentHash());
        snapshot.setContent(request.content());
        snapshotRepository.save(snapshot);

        watchedUrl.setLastCheckedAt(Instant.now());
        watchedUrlRepository.save(watchedUrl);

        if (changed) {
            Notification notification = new Notification();
            notification.setWatchedUrlId(watchedUrl.getId());
            notification.setUserId(watchedUrl.getUserId());
            notification.setMessage("Change detected on: " + watchedUrl.getLabel());
            notification.setSeen(false);
            notificationRepository.save(notification);
        }
    }
}
