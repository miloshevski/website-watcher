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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalService {

    private final WatchedUrlRepository watchedUrlRepository;
    private final SnapshotRepository snapshotRepository;
    private final NotificationRepository notificationRepository;

    public List<WatchedUrlResponse> getDueUrls() {
        return watchedUrlRepository.findDue()
                .stream()
                .map(w -> new WatchedUrlResponse(
                        w.getId(), w.getUrl(), w.getLabel(), w.getSelector(),
                        w.getCheckIntervalMinutes(), w.getActive(),
                        w.getCreatedAt(), w.getLastCheckedAt()
                ))
                .toList();
    }

    @Transactional
    public void submitSnapshot(SnapshotRequest request) {
        WatchedUrl watchedUrl = watchedUrlRepository.findById(request.watchedUrlId())
                .orElseThrow(() -> new IllegalArgumentException("WatchedUrl not found"));

        var latest = snapshotRepository.findTopByWatchedUrlIdOrderByCapturedAtDesc(watchedUrl.getId());
        boolean changed = latest.map(s -> !s.getContentHash().equals(request.contentHash())).orElse(true);

        Snapshot snapshot = new Snapshot();
        snapshot.setWatchedUrl(watchedUrl);
        snapshot.setContentHash(request.contentHash());
        snapshot.setContent(request.content());
        snapshotRepository.save(snapshot);

        watchedUrl.setLastCheckedAt(Instant.now());
        watchedUrlRepository.save(watchedUrl);

        if (changed) {
            Notification notification = new Notification();
            notification.setWatchedUrl(watchedUrl);
            notification.setUser(watchedUrl.getUser());
            notification.setMessage("Change detected on: " + watchedUrl.getLabel());
            notification.setSeen(false);
            notificationRepository.save(notification);
        }
    }
}
