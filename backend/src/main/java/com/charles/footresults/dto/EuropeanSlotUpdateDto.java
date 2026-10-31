package com.charles.footresults.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Creation / modification a la main d'une place europeenne. Un club choisi fige la place (plus de
 * remplissage automatique) ; sans club, elle revient au remplissage automatique.
 */
public record EuropeanSlotUpdateDto(
        @NotNull Integer season,
        @NotBlank String competitionCode,
        @NotNull @Min(1) @Max(6) Integer entryStage,
        @NotBlank String country,
        String route,
        Long teamId) {}
