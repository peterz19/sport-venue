package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Long> {

    List<Court> findByMerchantIdOrderBySortOrderAscIdAsc(Long merchantId);

    List<Court> findByMerchantIdAndVenueIdOrderBySortOrderAscIdAsc(Long merchantId, Long venueId);

    List<Court> findByMerchantIdAndStatusOrderBySortOrderAscIdAsc(Long merchantId, Court.CourtStatus status);

    List<Court> findByMerchantIdAndVenueIdAndStatusOrderBySortOrderAscIdAsc(
            Long merchantId, Long venueId, Court.CourtStatus status);

    Optional<Court> findByIdAndMerchantId(Long id, Long merchantId);
}
