package com.yowyob.user.application.port.out;

import com.yowyob.user.entity.UserProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for user profile persistence.
 */
public interface UserProfileRepositoryPort {
    Optional<UserProfile> findByUserId(UUID userId);
    UserProfile save(UserProfile profile);
    List<UserProfile> findAll();
    List<UserProfile> findByUpdatedAtAfter(LocalDateTime dateTime);
}
