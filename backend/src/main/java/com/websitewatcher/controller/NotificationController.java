package com.websitewatcher.controller;

import com.websitewatcher.dto.NotificationResponse;
import com.websitewatcher.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal UserDetails user,
                                                           @RequestParam(required = false) Boolean seen) {
        return ResponseEntity.ok(notificationService.getForUser(user.getUsername(), seen));
    }

    @PutMapping("/{id}/seen")
    public ResponseEntity<NotificationResponse> markSeen(@AuthenticationPrincipal UserDetails user,
                                                         @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markSeen(user.getUsername(), id));
    }
}
