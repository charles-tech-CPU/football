package com.charles.footresults.repository;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    Optional<Competition> findByCodeAndSeason(String code, Integer season);

    List<Competition> findByType(CompetitionType type);

    List<Competition> findByTypeAndSeason(CompetitionType type, Integer season);

    Optional<Competition> findFirstByTypeAndCountryAndSeason(CompetitionType type, String country, Integer season);

    /** Fusion de clubs (TeamService.merge) : le vainqueur fige a la cloture suit le club conserve. */
    @Modifying
    @Query("update Competition c set c.winnerTeam = :target where c.winnerTeam = :source")
    void reassignWinner(@Param("source") Team source, @Param("target") Team target);
}
