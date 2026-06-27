package com.yowyob.user.application.service;

import com.yowyob.user.application.port.in.UserUseCase;
import com.yowyob.user.application.port.out.SearchHistoryRepositoryPort;
import com.yowyob.user.application.port.out.UserProfileRepositoryPort;
import com.yowyob.user.dto.UserProfileDto;
import com.yowyob.user.entity.SearchHistory;
import com.yowyob.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing all user profile and search history use cases.
 * Delegates persistence to the output ports (no direct repository access).
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService implements UserUseCase {

    private final UserProfileRepositoryPort userProfileRepositoryPort;
    private final SearchHistoryRepositoryPort searchHistoryRepositoryPort;

    @Override
    public UserProfile getOrCreateProfile(UUID userId) {
        return userProfileRepositoryPort.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .userId(userId)
                            .build();
                    return userProfileRepositoryPort.save(newProfile);
                });
    }

    @Override
    public UserProfile updateProfile(UUID userId, UserProfileDto dto) {
        UserProfile profile = getOrCreateProfile(userId);

        if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
        if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
        if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null) profile.setAddress(dto.getAddress());
        if (dto.getCity() != null) profile.setCity(dto.getCity());
        if (dto.getCountry() != null) profile.setCountry(dto.getCountry());
        if (dto.getAvatarUrl() != null) profile.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getSocialLinksJson() != null) profile.setSocialLinksJson(dto.getSocialLinksJson());

        return userProfileRepositoryPort.save(profile);
    }

    @Override
    public List<UserProfile> findAllProfiles() {
        return userProfileRepositoryPort.findAll();
    }

    @Override
    public List<UserProfile> searchProfiles(LocalDateTime updatedAfter) {
        if (updatedAfter == null) {
            return userProfileRepositoryPort.findAll();
        }
        return userProfileRepositoryPort.findByUpdatedAtAfter(updatedAfter);
    }

    @Override
    public void addSearchHistory(UUID userId, String query) {
        if (query == null || query.isBlank()) return;
        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .query(query.trim())
                .build();
        searchHistoryRepositoryPort.save(history);
    }

    @Override
    public List<SearchHistory> getSearchHistory(UUID userId) {
        return searchHistoryRepositoryPort.findByUserIdOrderBySearchedAtDesc(userId);
    }

    @Override
    @Transactional
    public void clearSearchHistory(UUID userId) {
        searchHistoryRepositoryPort.deleteByUserId(userId);
    }
}
