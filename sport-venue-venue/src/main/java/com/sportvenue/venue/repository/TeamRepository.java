package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByMerchantIdOrderByIdDesc(Long merchantId);

    List<Team> findByMerchantIdAndStatusOrderByNameAsc(Long merchantId, Team.TeamStatus status);

    Optional<Team> findByIdAndMerchantId(Long id, Long merchantId);

    boolean existsByMerchantIdAndNameAndStatus(Long merchantId, String name, Team.TeamStatus status);
}
