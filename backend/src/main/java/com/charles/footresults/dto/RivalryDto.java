package com.charles.footresults.dto;

import com.charles.footresults.domain.Rivalry;

/** Rivalite entre deux clubs (pays : celui du premier club). */
public record RivalryDto(
        Long id, Long team1Id, String team1Name, Long team2Id, String team2Name, String country, String label) {

    public static RivalryDto from(Rivalry r) {
        return new RivalryDto(
                r.getId(),
                r.getTeam1().getId(),
                r.getTeam1().getName(),
                r.getTeam2().getId(),
                r.getTeam2().getName(),
                r.getTeam1().getCountry(),
                r.getLabel());
    }
}
