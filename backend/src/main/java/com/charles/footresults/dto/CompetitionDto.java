package com.charles.footresults.dto;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;

public record CompetitionDto(
        Long id,
        String code,
        String name,
        CompetitionType type,
        String country,
        Integer season,
        Integer ldcSlots,
        Integer elSlots,
        Integer eclSlots,
        Integer relegationSlots,
        Integer barrageSlots) {
    public static CompetitionDto from(Competition c) {
        return new CompetitionDto(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getType(),
                c.getCountry(),
                c.getSeason(),
                c.getLdcSlots(),
                c.getElSlots(),
                c.getEclSlots(),
                c.getRelegationSlots(),
                c.getBarrageSlots());
    }
}
