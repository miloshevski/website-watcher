package com.websitewatcher.repository;

import com.websitewatcher.entity.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotRepository extends JpaRepository<Snapshot, UUID> {
    Optional<Snapshot> findTopByWatchedUrlIdOrderByCapturedAtDesc(UUID watchedUrlId);
}
