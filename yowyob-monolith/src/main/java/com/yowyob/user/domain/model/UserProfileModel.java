package com.yowyob.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing a user's extended profile.
 * No persistence annotations — technology-agnostic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileModel {
    private UUID id;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
    private String avatarUrl;
    private String socialLinksJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
