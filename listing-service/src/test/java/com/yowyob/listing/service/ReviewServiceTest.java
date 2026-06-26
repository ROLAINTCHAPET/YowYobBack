package com.yowyob.listing.service;

import com.yowyob.listing.dto.ReviewRequest;
import com.yowyob.listing.dto.ReviewResponse;
import com.yowyob.listing.dto.ReviewSummary;
import com.yowyob.listing.entity.Listing;
import com.yowyob.listing.entity.Review;
import com.yowyob.listing.repository.ListingRepository;
import com.yowyob.listing.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Listing mockListing;
    private UUID listingId;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        mockListing = new Listing();
        mockListing.setId(listingId);
        mockListing.setTitle("Restaurant Test");
        mockListing.setAverageRating(0.0);
        mockListing.setReviewCount(0);
    }

    @Test
    @DisplayName("Doit créer un avis et mettre à jour la moyenne")
    void createReview_shouldSaveAndUpdateRating() {
        // ARRANGE
        ReviewRequest request = new ReviewRequest(5, "Excellent !", "user_001");

        when(listingRepository.findById(listingId))
            .thenReturn(Optional.of(mockListing));
        when(reviewRepository.existsByListingIdAndUserId(listingId, "user_001"))
            .thenReturn(false);
        when(reviewRepository.calculateAverageRating(listingId))
            .thenReturn(Optional.of(5.0));
        when(reviewRepository.countByListingId(listingId))
            .thenReturn(1L);

        // ACT
        ReviewResponse response = reviewService.createReview(listingId, request);

        // ASSERT
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent !");
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(listingRepository, times(1)).save(mockListing);
        assertThat(mockListing.getAverageRating()).isEqualTo(5.0);
        assertThat(mockListing.getReviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doit rejeter un doublon d'avis du même utilisateur")
    void createReview_shouldRejectDuplicateFromSameUser() {
        // ARRANGE
        ReviewRequest request = new ReviewRequest(3, "Deuxième tentative", "user_001");

        when(listingRepository.findById(listingId))
            .thenReturn(Optional.of(mockListing));
        when(reviewRepository.existsByListingIdAndUserId(listingId, "user_001"))
            .thenReturn(true); // déjà noté

        // ACT & ASSERT
        assertThatThrownBy(() -> reviewService.createReview(listingId, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("déjà soumis un avis");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit calculer correctement la moyenne avec plusieurs avis")
    void getSummary_shouldCalculateCorrectAverage() {
        // ARRANGE
        mockListing.setAverageRating(4.0);
        mockListing.setReviewCount(2);

        Review r1 = Review.builder().rating(5).userId("u1").listing(mockListing).build();
        Review r2 = Review.builder().rating(3).userId("u2").listing(mockListing).build();

        when(listingRepository.findById(listingId))
            .thenReturn(Optional.of(mockListing));
        when(reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId))
            .thenReturn(List.of(r1, r2));

        // ACT
        ReviewSummary summary = reviewService.getSummary(listingId);

        // ASSERT
        assertThat(summary.getAverageRating()).isEqualTo(4.0);
        assertThat(summary.getReviewCount()).isEqualTo(2);
        assertThat(summary.getRatingDistribution().get(5)).isEqualTo(1L);
        assertThat(summary.getRatingDistribution().get(3)).isEqualTo(1L);
        assertThat(summary.getRatingDistribution().get(1)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Doit lever une exception si le commerce n'existe pas")
    void createReview_shouldThrowIfListingNotFound() {
        // ARRANGE
        when(listingRepository.findById(any()))
            .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() ->
            reviewService.createReview(UUID.randomUUID(),
                new ReviewRequest(4, "Test", "user_001")))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
