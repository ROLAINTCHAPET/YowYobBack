package com.yowyob.user.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserProfileDto {
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
}
