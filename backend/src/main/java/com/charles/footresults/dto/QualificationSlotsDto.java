package com.charles.footresults.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QualificationSlotsDto(
        @NotNull @Min(0) Integer ldcSlots,
        @NotNull @Min(0) Integer elSlots,
        @NotNull @Min(0) Integer eclSlots
) {
}
