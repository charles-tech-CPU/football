package com.charles.footresults.dto;

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
        Integer score2
) {
}
