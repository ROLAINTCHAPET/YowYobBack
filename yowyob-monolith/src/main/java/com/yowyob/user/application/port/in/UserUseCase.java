package com.yowyob.user.application.port.in;

import com.yowyob.user.dto.UserProfileDto;
import com.yowyob.user.entity.UserProfile;
import com.yowyob.user.entity.SearchHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Input port (use case) for all user profile and search history operations.
 */
public interface UserUseCase {
    UserProfile getOrCreateProfile(UUID userId);
    UserProfile updateProfile(UUID userId, UserProfileDto dto);
    List<UserProfile> findAllProfiles();
    List<UserProfile> searchProfiles(LocalDateTime updatedAfter);

    void addSearchHistory(UUID userId, String query);
    List<SearchHistory> getSearchHistory(UUID userId);
    void clearSearchHistory(UUID userId);
}
