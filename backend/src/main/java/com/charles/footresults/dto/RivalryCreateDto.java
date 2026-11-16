package com.charles.footresults.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nouvelle rivalite : les deux clubs (dans n'importe quel ordre) et un nom facultatif. */
public record RivalryCreateDto(
        @NotNull Long team1Id,
        @NotNull Long team2Id,
        @Size(max = 80) String label) {}
