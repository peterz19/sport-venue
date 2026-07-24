package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    Optional<MatchResult> findByIdAndMerchantId(Long id, Long merchantId);

    Optional<MatchResult> findByBookingIdAndMerchantId(Long bookingId, Long merchantId);

    boolean existsByBookingId(Long bookingId);

    List<MatchResult> findByMerchantIdOrderByCreateTimeDesc(Long merchantId);

    @Query("SELECT m FROM MatchResult m WHERE m.merchantId = :merchantId " +
           "AND (m.homeTeamId = :teamId OR m.awayTeamId = :teamId) ORDER BY m.createTime DESC")
    List<MatchResult> findByTeam(@Param("merchantId") Long merchantId, @Param("teamId") Long teamId);
}
