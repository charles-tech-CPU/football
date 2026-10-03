package com.charles.footresults.dto;

/**
 * Ligne du classement final projete (cf. StandingsService.computeProjectedStandings) : memes
 * colonnes que StandingRowDto pour le classement actuel (MJ, V, N, D, buts, points reels), plus
 * la projection et la fourchette de rangs encore atteignables.
 */
public record ProjectedStandingRowDto(
        Long teamId,
        String teamName,
        String teamLogoPath,
        String teamCountry,
        int played,
        int won,
        int drawn,
        int lost,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int points,
        String group,
        /** Matchs restant a jouer (statut different de COMPLETED). */
        int remaining,
        /** Points actuels + moyenne de points par match x matchs restants (non arrondi). */
        double projectedPoints,
        /** Rang actuel dans le groupe (1 = premier), pour afficher la progression. */
        int currentRank,
        /** Meilleur / pire rang final encore mathematiquement possible dans le groupe. */
        int bestRank,
        int worstRank) {

    public static ProjectedStandingRowDto of(
            StandingRowDto row, int remaining, double projectedPoints, int currentRank, int bestRank, int worstRank) {
        return new ProjectedStandingRowDto(
                row.teamId(),
                row.teamName(),
                row.teamLogoPath(),
                row.teamCountry(),
                row.played(),
                row.won(),
                row.drawn(),
                row.lost(),
                row.goalsFor(),
                row.goalsAgainst(),
                row.goalDifference(),
                row.points(),
                row.group(),
                remaining,
                projectedPoints,
                currentRank,
                bestRank,
                worstRank);
    }
}
