package com.charles.footresults.repository;

import com.charles.footresults.domain.Honour;
import com.charles.footresults.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HonourRepository extends JpaRepository<Honour, Long> {
    /** Saisons les plus recentes d'abord ; a saison egale, l'ordre d'import (vainqueur "outright" de l'Intertoto). */
    @EntityGraph(attributePaths = {"winnerTeam", "runnerUpTeam", "thirdTeam"})
    List<Honour> findByScopeOrderBySeasonEndDescIdAsc(String scope);

    /** Fusion de clubs (TeamService.merge) : le palmares suit le club conserve. */
    @Modifying
    @Query("update Honour h set h.winnerTeam = :target where h.winnerTeam = :source")
    void reassignWinner(@Param("source") Team source, @Param("target") Team target);

    @Modifying
    @Query("update Honour h set h.runnerUpTeam = :target where h.runnerUpTeam = :source")
    void reassignRunnerUp(@Param("source") Team source, @Param("target") Team target);

    @Modifying
    @Query("update Honour h set h.thirdTeam = :target where h.thirdTeam = :source")
    void reassignThird(@Param("source") Team source, @Param("target") Team target);

    /** Lignes de palmares d'une equipe, vainqueur, finaliste ou 3e (suppression d'une equipe). */
    @Query(
            "select count(h) from Honour h where h.winnerTeam.id = :teamId or h.runnerUpTeam.id = :teamId or h.thirdTeam.id = :teamId")
    long countByTeam(@Param("teamId") Long teamId);

    @Query("select distinct h.scope from Honour h order by h.scope")
    List<String> findScopes();
}
