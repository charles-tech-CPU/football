package com.charles.footresults.repository;

import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByCompetitionIdOrderByDateAscTimeAsc(Long competitionId);

    List<Match> findByCompetitionIdAndStatusOrderByDateAscTimeAsc(Long competitionId, MatchStatus status);

    /** Reserve aux classements de phase (ex: phase de ligue LDC/EL/EC) : ne tient compte que des matchs dont le round_label contient ce fragment. */
    List<Match> findByCompetitionIdAndStatusAndRoundLabelContainingIgnoreCaseOrderByDateAscTimeAsc(
            Long competitionId, MatchStatus status, String roundLabelPart);

    List<Match> findByTeam1_IdOrTeam2_IdOrderByDateDesc(Long team1Id, Long team2Id);

    /** Tous les matchs LDC/EL/EC (tous tours, tous statuts) pour le recalcul des points et du statut "encore en course" UEFA (cf. UefaRankingService). */
    List<Match> findByCompetition_CodeIn(List<String> competitionCodes);

    /** Matchs reportes/suspendus, toutes competitions confondues : jamais borne par une limite,
     * contrairement a findUpcoming qui ne renvoie que les N prochains matchs par date. */
    List<Match> findByStatusInOrderByDateAscTimeAsc(List<MatchStatus> statuses);

    /** Calendrier international (selections nationales, toutes confederations) : volume raisonnable
     * pour une seule saison (quelques centaines de matchs), pas besoin de pagination cote serveur. */
    List<Match> findByCompetition_TypeOrderByDateAscTimeAsc(CompetitionType type);

    /** Matchs sans date (tours de qualif. coupes d'Europe sans date exploitable) rejetes en fin de liste. */
    @Query("SELECT m FROM Match m WHERE m.status = :status ORDER BY m.date DESC NULLS LAST, m.time DESC NULLS LAST")
    List<Match> findRecentByStatus(@Param("status") MatchStatus status, Pageable pageable);

    /**
     * Matchs a venir (SCHEDULED/FORFEIT ; les reportes/suspendus ont leur propre onglet dedie),
     * avec filtre optionnel par type de competition (championnat/coupe nationale) ou par code
     * (LDC/EL/EC) et vraie pagination cote base (le calendrier agrege ~8000 matchs toutes
     * competitions confondues, hors de question de tout charger cote client a chaque fois).
     */
    @Query("SELECT m FROM Match m WHERE m.status NOT IN :excludedStatuses "
            + "AND (:type IS NULL OR m.competition.type = :type) "
            + "AND (:code IS NULL OR m.competition.code = :code) "
            + "ORDER BY m.date ASC NULLS LAST, m.time ASC NULLS LAST")
    Page<Match> findUpcomingFiltered(@Param("excludedStatuses") List<MatchStatus> excludedStatuses,
                                      @Param("type") CompetitionType type,
                                      @Param("code") String code,
                                      Pageable pageable);
}
