package com.charles.footresults.repository;

import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByCompetitionIdOrderByDateAscTimeAsc(Long competitionId);

    List<Match> findByCompetitionIdAndStatusOrderByDateAscTimeAsc(Long competitionId, MatchStatus status);

    List<Match> findByTeam1_IdOrTeam2_IdOrderByDateDesc(Long team1Id, Long team2Id);

    /** Matchs sans date (tours de qualif. coupes d'Europe sans date exploitable) rejetes en fin de liste. */
    @Query("SELECT m FROM Match m WHERE m.status = :status ORDER BY m.date DESC NULLS LAST, m.time DESC NULLS LAST")
    List<Match> findRecentByStatus(@Param("status") MatchStatus status, Pageable pageable);

    /** Tous les matchs pas encore joues (SCHEDULED/POSTPONED/SUSPENDED/FORFEIT), du plus proche au plus lointain. */
    @Query("SELECT m FROM Match m WHERE m.status <> :completed ORDER BY m.date ASC NULLS LAST, m.time ASC NULLS LAST")
    List<Match> findUpcoming(@Param("completed") MatchStatus completed, Pageable pageable);
}
