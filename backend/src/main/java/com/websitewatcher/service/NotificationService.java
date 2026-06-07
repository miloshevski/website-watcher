package com.websitewatcher.service;

import com.websitewatcher.dto.NotificationResponse;
import com.websitewatcher.entity.User;
import com.websitewatcher.repository.NotificationRepository;
import com.websitewatcher.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getForUser(String email, Boolean seenFilter) {
        User user = findUser(email);
        var notifications = seenFilter != null
                ? notificationRepository.findByUserIdAndSeen(user.getId(), seenFilter)
                : notificationRepository.findByUserId(user.getId());
        return notifications.stream().map(this::toResponse).toList();
    }

    public NotificationResponse markSeen(String email, UUID id) {
        User user = findUser(email);
        var notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        notification.setSeen(true);
        return toResponse(notificationRepository.save(notification));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(com.websitewatcher.entity.Notification n) {
        return new NotificationResponse(
                n.getId(), n.getWatchedUrl().getId(),
                n.getMessage(), n.getSeen(), n.getCreatedAt()
        );
    }
}
