package com.yowyob.user.repository;

import com.yowyob.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);

    java.util.List<UserProfile> findByUpdatedAtAfter(java.time.LocalDateTime updatedAt);
}
