package com.websitewatcher.repository;

import com.websitewatcher.entity.Snapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SnapshotRepository extends MongoRepository<Snapshot, String> {
    Optional<Snapshot> findTopByWatchedUrlIdOrderByCapturedAtDesc(String watchedUrlId);
    void deleteByWatchedUrlId(String watchedUrlId);
}
