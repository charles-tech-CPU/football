package com.charles.footresults.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un point de la courbe d'evolution du coefficient UEFA d'un club ou d'un pays (cf.
 * UefaRankingService.findClubHistory / findCountryHistory).
 */
public record UefaHistoryPointDto(
        /** Date du ou des matchs ; null pour le point de depart (matchs de qualif. sans date inclus). */
        LocalDate date,
        /** Points de la saison en cours cumules jusqu'a cette date incluse. */
        BigDecimal seasonPoints,
        /** Coefficient sur 5 saisons a cette date (4 saisons passees importees + saison en cours). */
        BigDecimal coefficient) {}
