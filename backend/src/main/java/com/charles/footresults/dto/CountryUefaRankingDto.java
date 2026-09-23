package com.charles.footresults.dto;

import java.math.BigDecimal;

public record CountryUefaRankingDto(
        Long id,
        Integer rank,
        String country,
        BigDecimal total,
        /** Recalcule a la volee a partir des matchs "PHASE DE LIGUE" COMPLETED des clubs du pays, jamais la valeur importee (cf. UefaRankingService). */
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
        String colorCode) {}
