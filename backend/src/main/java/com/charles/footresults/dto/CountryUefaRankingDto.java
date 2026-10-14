package com.charles.footresults.dto;

import java.math.BigDecimal;

public record CountryUefaRankingDto(
        Long id,
        Integer rank,
        String country,
        /** Coefficient sur 5 saisons, avec les points 2027 recalcules (cf. UefaRankingService), jamais le total importe. */
        BigDecimal total,
        /** Recalcule a la volee a partir des matchs LDC/EL/EC COMPLETED des clubs du pays, jamais la valeur importee (cf. UefaRankingService). */
        BigDecimal points2027,
        BigDecimal points2026,
        BigDecimal points2025,
        BigDecimal points2024,
        BigDecimal points2023,
        /** Nombre de clubs du pays encore en course dans chaque coupe, recalcule en direct (elimination = exclu), pas la photo d'import. */
        Integer ldcNow,
        Integer elNow,
        Integer ecNow,
        Integer ldcDebut,
        Integer elDebut,
        Integer ecDebut,
        Integer nb2027,
        Integer nb2026,
        Integer nb2025,
        Integer nb2024,
        Integer nb2023,
        /** "BLUE" (LDC), "ORANGE" (EL), "YELLOW" (EC) ou "RED" (aucun) : cf. UefaRankingService.colorCode. */
        String colorCode) {

    public CountryUefaRankingDto withRank(int newRank) {
        return new CountryUefaRankingDto(
                id,
                newRank,
                country,
                total,
                points2027,
                points2026,
                points2025,
                points2024,
                points2023,
                ldcNow,
                elNow,
                ecNow,
                ldcDebut,
                elDebut,
                ecDebut,
                nb2027,
                nb2026,
                nb2025,
                nb2024,
                nb2023,
                colorCode);
    }
}
