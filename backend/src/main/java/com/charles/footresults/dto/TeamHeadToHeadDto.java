package com.charles.footresults.dto;

import com.charles.footresults.domain.CompetitionType;
import java.util.List;

/**
 * Tete-a-tete entre deux equipes (cf. TeamHeadToHeadService), toujours du point de vue de
 * "team" face a "opponent". Une seance de tirs au but ne change pas le resultat (match nul),
 * elle est comptee a part dans penaltyWins / penaltyLosses.
 */
public record TeamHeadToHeadDto(
        TeamDto team,
        TeamDto opponent,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        int penaltyWins,
        int penaltyLosses,
        /** Plus large victoire de chaque cote (le plus de buts marques a ecart egal) ; null si aucune. */
        MatchDto biggestWin,
        MatchDto biggestLoss,
        /** Bilan par competition, la plus recemment disputee d'abord. */
        List<CompetitionRecord> byCompetition,
        /** Matchs joues, du plus recent au plus ancien. */
        List<MatchDto> playedMatches,
        /** Matchs a venir, du plus proche au plus lointain. */
        List<MatchDto> upcomingMatches) {

    public record CompetitionRecord(
            Long competitionId,
            String competitionName,
            CompetitionType competitionType,
            String competitionCountry,
            int played,
            int wins,
            int draws,
            int losses) {}
}
