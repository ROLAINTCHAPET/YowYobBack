package com.yowyob.auth.infrastructure.adapter.out.persistence;

import com.yowyob.auth.domain.model.User;
import com.yowyob.auth.entity.User;

public class UserMapper {

    public static com.yowyob.auth.entity.User toEntity(com.yowyob.auth.domain.model.User domain) {
        if (domain == null) return null;
        return com.yowyob.auth.entity.User.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .phone(domain.getPhone())
                .avatarUrl(domain.getAvatarUrl())
                .role(domain.getRole() != null ? com.yowyob.auth.entity.User.Role.valueOf(domain.getRole().name()) : null)
                .emailVerified(domain.getEmailVerified())
                .status(domain.getStatus() != null ? com.yowyob.auth.entity.User.Status.valueOf(domain.getStatus().name()) : null)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static com.yowyob.auth.domain.model.User toDomain(com.yowyob.auth.entity.User entity) {
        if (entity == null) return null;
        return com.yowyob.auth.domain.model.User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole() != null ? com.yowyob.auth.domain.model.User.Role.valueOf(entity.getRole().name()) : null)
                .emailVerified(entity.getEmailVerified())
                .status(entity.getStatus() != null ? com.yowyob.auth.domain.model.User.Status.valueOf(entity.getStatus().name()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
