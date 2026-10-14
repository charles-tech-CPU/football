package com.charles.footresults.dto;

import java.math.BigDecimal;

public record ClubUefaRankingDto(
        Long id,
        Integer rank,
        Long teamId,
        String clubName,
        String teamLogoPath,
        String country,
        /** Coupe d'Europe encore jouee ("LDC"/"EL"/"EC") ; devient null des que le club en est elimine (cf. UefaRankingService), pas seulement s'il n'y a jamais participe. */
        String currentCup,
        /** Somme des 5 saisons, avec les points 2027 recalcules (cf. UefaRankingService), jamais le total importe. */
        BigDecimal total,
        /** Recalcule a la volee a partir des matchs LDC/EL/EC COMPLETED, jamais la valeur importee (cf. UefaRankingService). */
        BigDecimal points2027,
        BigDecimal points2026,
        BigDecimal points2025,
        BigDecimal points2024,
        BigDecimal points2023) {

    public ClubUefaRankingDto withRank(int newRank) {
        return new ClubUefaRankingDto(
                id,
                newRank,
                teamId,
                clubName,
                teamLogoPath,
                country,
                currentCup,
                total,
                points2027,
                points2026,
                points2025,
                points2024,
                points2023);
    }
}
