package com.yowyob.user.infrastructure.adapter.out.persistence;

import com.yowyob.user.application.port.out.UserProfileRepositoryPort;
import com.yowyob.user.entity.UserProfile;
import com.yowyob.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementing the UserProfileRepositoryPort output port.
 */
@Component
@RequiredArgsConstructor
public class JpaUserProfileRepositoryAdapter implements UserProfileRepositoryPort {

    private final UserProfileRepository userProfileRepository;

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    @Override
    public List<UserProfile> findAll() {
        return userProfileRepository.findAll();
    }

    @Override
    public List<UserProfile> findByUpdatedAtAfter(LocalDateTime dateTime) {
        return userProfileRepository.findByUpdatedAtAfter(dateTime);
    }
}
