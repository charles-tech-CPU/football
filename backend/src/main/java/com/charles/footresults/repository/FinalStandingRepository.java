package com.charles.footresults.repository;

import com.charles.footresults.domain.FinalStanding;
import com.charles.footresults.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinalStandingRepository extends JpaRepository<FinalStanding, Long> {

    List<FinalStanding> findByCompetitionIdOrderByFinalRankAsc(Long competitionId);

    @Modifying
    @Query("delete from FinalStanding f where f.competition.id = :competitionId")
    void deleteByCompetitionId(@Param("competitionId") Long competitionId);

    /** Suppression d'une equipe (cf. TeamService.delete). */
    long countByTeamId(Long teamId);

    /** Fusion de clubs (TeamService.merge) : le classement fige suit le club conserve. */
    @Modifying
    @Query("update FinalStanding f set f.team = :target where f.team = :source")
    void reassignTeam(@Param("source") Team source, @Param("target") Team target);
}
