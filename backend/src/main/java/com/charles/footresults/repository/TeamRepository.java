package com.charles.footresults.repository;

import com.charles.footresults.domain.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    /** Equipes ayant au moins un match dans cette competition (championnat, coupe ou LDC/EL/EC). */
    @Query("""
            SELECT DISTINCT t FROM Team t
            JOIN Match m ON m.team1 = t OR m.team2 = t
            WHERE m.competition.id = :competitionId
            """)
    List<Team> findByCompetitionId(@Param("competitionId") Long competitionId);
}
