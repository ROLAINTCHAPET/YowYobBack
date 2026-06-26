package com.yowyob.listing.controller;

import com.yowyob.listing.dto.ReviewRequest;
import com.yowyob.listing.dto.ReviewResponse;
import com.yowyob.listing.dto.ReviewSummary;
import com.yowyob.listing.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings/{listingId}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    // POST /api/listings/{listingId}/reviews
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID listingId,
            @RequestBody @Valid ReviewRequest request) {

        ReviewResponse response = reviewService.createReview(listingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/listings/{listingId}/reviews
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable UUID listingId) {

        return ResponseEntity.ok(reviewService.getReviews(listingId));
    }

    // GET /api/listings/{listingId}/reviews/summary
    @GetMapping("/summary")
    public ResponseEntity<ReviewSummary> getSummary(
            @PathVariable UUID listingId) {

        return ResponseEntity.ok(reviewService.getSummary(listingId));
    }

    // DELETE /api/listings/{listingId}/reviews/{reviewId}
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID listingId,
            @PathVariable UUID reviewId,
            @RequestParam String userId) {

        reviewService.deleteReview(listingId, reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
