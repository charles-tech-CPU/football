package com.charles.footresults.repository;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    Optional<Competition> findByCodeAndSeason(String code, Integer season);

    List<Competition> findByType(CompetitionType type);
}
