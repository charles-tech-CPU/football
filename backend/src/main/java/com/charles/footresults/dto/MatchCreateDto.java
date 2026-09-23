package com.charles.footresults.dto;

import com.charles.footresults.domain.MatchStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record MatchCreateDto(
        @NotNull Long competitionId,
        @NotNull String roundLabel,
        LocalDate date,
        LocalTime time,
        @NotNull Long team1Id,
        @NotNull Long team2Id,
        Integer score1,
        Integer score2,
        /** Tirs au but (coupe a elimination directe terminee a egalite), optionnel. */
        Integer penaltyScore1,
        Integer penaltyScore2,
        /** Optionnel : force un statut (REPORTE/SUSPENDU/FORFAIT). Si null, deduit des scores (COMPLETED/SCHEDULED). */
        MatchStatus status) {}
