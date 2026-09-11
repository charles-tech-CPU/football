package com.charles.footresults.dto;

public record StandingRowDto(
        Long teamId,
        String teamName,
        int played,
        int won,
        int drawn,
        int lost,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int points,
        /** Groupe de 2eme phase (ex: "Championnat", "Relegation") ; null si la competition n'est pas scindee. */
        String group
) {
}
