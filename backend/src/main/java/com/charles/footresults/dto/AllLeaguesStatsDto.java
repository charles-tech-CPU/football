package com.charles.footresults.dto;

import java.util.List;

/** Statistiques de tous les championnats de la saison reunis (cf. StatsService.allLeaguesStats). */
public record AllLeaguesStatsDto(
        /** Memes statistiques que pour une competition, sur l'ensemble des matchs de championnat. */
        CompetitionStatsDto overall,
        /** Bilan de chaque championnat, par pays. */
        List<LeagueSummary> leagues) {

    public record LeagueSummary(
            Long competitionId,
            String name,
            String country,
            int matchesPlayed,
            int goals,
            int homeWins,
            int draws,
            int awayWins) {}
}
