package com.charles.footresults.dto;

import com.charles.footresults.domain.CompetitionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompetitionCreateDto(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull CompetitionType type,
        String country,
        @NotNull Integer season) {}
