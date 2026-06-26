package com.yowyob.listing.service;

import com.yowyob.listing.dto.ReviewRequest;
import com.yowyob.listing.dto.ReviewResponse;
import com.yowyob.listing.dto.ReviewSummary;
import com.yowyob.listing.entity.Listing;
import com.yowyob.listing.entity.Review;
import com.yowyob.listing.repository.ListingRepository;
import com.yowyob.listing.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ListingRepository listingRepository;

    /**
     * Crée un nouvel avis pour un commerce
     */
    public ReviewResponse createReview(UUID listingId, ReviewRequest request) {

        // 1. Vérifie que le commerce existe
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Commerce introuvable : " + listingId));

        // 2. Vérifie que l'utilisateur n'a pas déjà noté ce commerce
        if (reviewRepository.existsByListingIdAndUserId(listingId, request.getUserId())) {
            throw new IllegalStateException(
                "Vous avez déjà soumis un avis pour ce commerce");
        }

        // 3. Crée l'avis
        Review review = Review.builder()
            .rating(request.getRating())
            .comment(request.getComment())
            .userId(request.getUserId())
            .listing(listing)
            .build();

        reviewRepository.save(review);

        // 4. Recalcule et met à jour la moyenne dans Listing
        updateListingRating(listing);

        log.info("Avis créé pour le commerce {} par l'utilisateur {}",
            listingId, request.getUserId());

        return toResponse(review);
    }

    /**
     * Retourne tous les avis d'un commerce
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(UUID listingId) {
        if (!listingRepository.existsById(listingId)) {
            throw new EntityNotFoundException("Commerce introuvable : " + listingId);
        }

        return reviewRepository
            .findByListingIdOrderByCreatedAtDesc(listingId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Retourne la moyenne et la distribution des notes
     */
    @Transactional(readOnly = true)
    public ReviewSummary getSummary(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Commerce introuvable : " + listingId));

        // Distribution : combien d'avis par note (1 à 5)
        List<Review> reviews =
            reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId);

        Map<Integer, Long> distribution = reviews.stream()
            .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        // S'assure que toutes les notes 1-5 sont présentes (même à 0)
        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }

        return ReviewSummary.builder()
            .averageRating(listing.getAverageRating())
            .reviewCount(listing.getReviewCount())
            .ratingDistribution(distribution)
            .build();
    }

    /**
     * Supprime un avis (l'utilisateur ne peut supprimer que le sien)
     */
    public void deleteReview(UUID listingId, UUID reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Avis introuvable : " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("Vous ne pouvez supprimer que vos propres avis");
        }

        reviewRepository.delete(review);

        // Recalcule la moyenne après suppression
        Listing listing = review.getListing();
        updateListingRating(listing);

        log.info("Avis {} supprimé par l'utilisateur {}", reviewId, userId);
    }

    // ── Méthodes privées ─────────────────────────────────────────

    private void updateListingRating(Listing listing) {
        double avg = reviewRepository
            .calculateAverageRating(listing.getId())
            .orElse(0.0);

        long count = reviewRepository.countByListingId(listing.getId());

        // Arrondi à 1 décimale — ex: 4.3 au lieu de 4.333333
        listing.setAverageRating(Math.round(avg * 10.0) / 10.0);
        listing.setReviewCount((int) count);
        listingRepository.save(listing);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
            .id(review.getId())
            .rating(review.getRating())
            .comment(review.getComment())
            .userId(review.getUserId())
            .createdAt(review.getCreatedAt())
            .build();
    }
}
