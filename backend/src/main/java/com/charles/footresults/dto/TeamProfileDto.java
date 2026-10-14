package com.charles.footresults.dto;

import java.math.BigDecimal;
import java.util.List;

/** Fiche club : situation actuelle et matchs, toutes competitions confondues (cf. TeamProfileService). */
public record TeamProfileDto(
        TeamDto team,
        /** Championnat de la saison la plus recente joue par le club ; null s'il n'en joue aucun. */
        LeagueStanding league,
        /** Ligne du classement UEFA des clubs (rangs recalcules) ; null si le club n'y figure pas. */
        UefaStanding uefa,
        /** Resultats des derniers matchs joues, du plus recent au plus ancien : "W", "D" ou "L". */
        List<String> form,
        /** Serie en cours ; null si aucun match joue. */
        Streak streak,
        List<MatchDto> recentMatches,
        List<MatchDto> upcomingMatches) {

    public record LeagueStanding(
            Long competitionId,
            String competitionName,
            /** null si le club n'a encore joue aucun match de championnat. */
            Integer rank,
            int teamCount,
            int played,
            int points,
            int goalsFor,
            int goalsAgainst) {}

    public record UefaStanding(
            Long rankingId, int rank, Integer startRank, BigDecimal total, BigDecimal points2027, String currentCup) {}

    /**
     * result/length : dernier resultat et nombre de matchs consecutifs avec ce resultat ;
     * unbeaten : matchs consecutifs sans defaite (0 si le dernier match est perdu).
     */
    public record Streak(String result, int length, int unbeaten) {}
}
