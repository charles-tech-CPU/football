package com.charles.footresults.dto;

import java.util.List;

/** Statistiques d'une competition, recalculees a partir de ses matchs joues (cf. StatsService). */
public record CompetitionStatsDto(
        int matchesPlayed,
        int goals,
        int homeWins,
        int draws,
        int awayWins,
        /** Buts marques, du plus grand au plus petit. */
        List<TeamValue> bestAttacks,
        /** Buts encaisses, du plus petit au plus grand. */
        List<TeamValue> bestDefenses,
        /** Plus grands ecarts de buts. */
        List<MatchDto> biggestWins,
        /** Plus grand nombre de buts dans un match. */
        List<MatchDto> highestScoring,
        /** Series en cours (nombre de matchs) : sans defaite, victoires, sans victoire. */
        List<TeamValue> unbeatenRuns,
        List<TeamValue> winRuns,
        List<TeamValue> winlessRuns) {

    public record TeamValue(Long teamId, String teamName, String teamLogoPath, int played, int value) {}
}
