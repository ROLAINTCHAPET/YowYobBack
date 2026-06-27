package com.yowyob.user.infrastructure.adapter.in.web;

import com.yowyob.user.application.port.in.UserUseCase;
import com.yowyob.user.dto.SearchHistoryDto;
import com.yowyob.user.dto.UserProfileDto;
import com.yowyob.user.entity.SearchHistory;
import com.yowyob.user.entity.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller adapter for user profile and search history endpoints.
 * Delegates all operations to the UserUseCase input port.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Service", description = "Operations for extended user profiles")
public class UserControllerAdapter {

    private final UserUseCase userUseCase;

    @Operation(summary = "Get My Profile", description = "Get profile of the currently authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userUseCase.getOrCreateProfile(UUID.fromString(userId)));
    }

    @Operation(summary = "Update My Profile", description = "Update profile of the currently authenticated user")
    @PutMapping("/me")
    public ResponseEntity<UserProfile> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UserProfileDto dto) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userUseCase.updateProfile(UUID.fromString(userId), dto));
    }

    @Operation(summary = "Get Public Profile", description = "Get public profile by User ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userUseCase.getOrCreateProfile(userId));
    }

    @Operation(summary = "Get All Users", description = "Get all user profiles (For Crawler)")
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userUseCase.findAllProfiles());
    }

    @Operation(summary = "Search Documents", description = "Internal endpoint for Crawler Service to fetch documents")
    @GetMapping("/search/documents")
    public ResponseEntity<List<UserProfile>> searchDocuments(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAfter) {
        return ResponseEntity.ok(userUseCase.searchProfiles(updatedAfter));
    }

    @Operation(summary = "Add Search History", description = "Add a query to user's search history")
    @PostMapping("/history")
    public ResponseEntity<Void> addSearchHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SearchHistoryDto dto) {
        if (userId == null) return ResponseEntity.status(401).build();
        userUseCase.addSearchHistory(UUID.fromString(userId), dto.getQuery());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get Search History", description = "Get user's search history")
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistory>> getSearchHistory(@RequestHeader("X-User-Id") String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userUseCase.getSearchHistory(UUID.fromString(userId)));
    }

    @Operation(summary = "Clear Search History", description = "Clear user's search history")
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearSearchHistory(@RequestHeader("X-User-Id") String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        userUseCase.clearSearchHistory(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get User Favorites", description = "Stubbed endpoint for favorites")
    @GetMapping("/favorites")
    public ResponseEntity<List<Object>> getUserFavorites(@RequestHeader("X-User-Id") String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(new ArrayList<>());
    }

    @Operation(summary = "Get User Messages Count", description = "Stubbed endpoint for messages count")
    @GetMapping("/messages/count")
    public ResponseEntity<Map<String, Integer>> getUserMessagesCount(@RequestHeader("X-User-Id") String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("count", 0));
    }

    @GetMapping("/health")
    public String health() {
        return "User Service is running!";
    }
}
