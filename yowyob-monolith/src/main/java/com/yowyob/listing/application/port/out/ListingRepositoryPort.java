package com.yowyob.listing.application.port.out;

import com.yowyob.listing.domain.model.Listing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface ListingRepositoryPort {
    Listing save(Listing listing);
    List<Listing> findAll();
    List<Listing> findByUpdatedAtAfter(LocalDateTime updatedAtAfter);
    Optional<Listing> findById(UUID id);
    List<Listing> findBySellerId(UUID sellerId);
    void delete(Listing listing);
}
