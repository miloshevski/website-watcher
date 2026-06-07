package com.websitewatcher.controller;

import com.websitewatcher.dto.SnapshotRequest;
import com.websitewatcher.dto.WatchedUrlResponse;
import com.websitewatcher.service.InternalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final InternalService internalService;

    @GetMapping("/watched-urls/due")
    public ResponseEntity<List<WatchedUrlResponse>> getDue() {
        return ResponseEntity.ok(internalService.getDueUrls());
    }

    @PostMapping("/snapshots")
    public ResponseEntity<Void> submitSnapshot(@Valid @RequestBody SnapshotRequest request) {
        internalService.submitSnapshot(request);
        return ResponseEntity.ok().build();
    }
}
