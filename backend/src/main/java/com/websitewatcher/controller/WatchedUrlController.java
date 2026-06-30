package com.websitewatcher.controller;

import com.websitewatcher.dto.WatchedUrlRequest;
import com.websitewatcher.dto.WatchedUrlResponse;
import com.websitewatcher.service.WatchedUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watched-urls")
@RequiredArgsConstructor
public class WatchedUrlController {

    private final WatchedUrlService watchedUrlService;

    @GetMapping
    public ResponseEntity<List<WatchedUrlResponse>> list(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(watchedUrlService.getForUser(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<WatchedUrlResponse> create(@AuthenticationPrincipal UserDetails user,
                                                     @Valid @RequestBody WatchedUrlRequest request) {
        return ResponseEntity.ok(watchedUrlService.create(user.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WatchedUrlResponse> update(@AuthenticationPrincipal UserDetails user,
                                                     @PathVariable String id,
                                                     @Valid @RequestBody WatchedUrlRequest request) {
        return ResponseEntity.ok(watchedUrlService.update(user.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user,
                                       @PathVariable String id) {
        watchedUrlService.delete(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
