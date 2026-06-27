package com.yowyob.listing.infrastructure.adapter.out.persistence;

import com.yowyob.listing.application.port.out.ListingRepositoryPort;
import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaListingRepositoryAdapter implements ListingRepositoryPort {

    private final ListingRepository listingRepository;

    @Override
    public Listing save(Listing listing) {
        com.yowyob.listing.entity.Listing entity = ListingMapper.toEntity(listing);
        com.yowyob.listing.entity.Listing savedEntity = listingRepository.save(entity);
        return ListingMapper.toDomain(savedEntity);
    }

    @Override
    public List<Listing> findAll() {
        return listingRepository.findAll().stream()
                .map(ListingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> findByUpdatedAtAfter(LocalDateTime updatedAtAfter) {
        return listingRepository.findByUpdatedAtAfter(updatedAfterAfter).stream()
                .map(ListingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return listingRepository.findById(id).map(ListingMapper::toDomain);
    }

    @Override
    public List<Listing> findBySellerId(UUID sellerId) {
        return listingRepository.findBySellerId(sellerId).stream()
                .map(ListingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Listing listing) {
        listingRepository.delete(ListingMapper.toEntity(listing));
    }
}
