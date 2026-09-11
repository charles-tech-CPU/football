package com.charles.footresults.dto;

import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchDto(
        Long id,
        Long competitionId,
        String competitionCode,
        String competitionName,
        String competitionCountry,
        String roundLabel,
        LocalDate date,
        LocalTime time,
        Long team1Id,
        String team1Name,
        Long team2Id,
        String team2Name,
        Integer score1,
        Integer score2,
        MatchStatus status
) {
    public static MatchDto from(Match m) {
        return new MatchDto(
                m.getId(),
                m.getCompetition().getId(),
                m.getCompetition().getCode(),
                m.getCompetition().getName(),
                m.getCompetition().getCountry(),
                m.getRoundLabel(),
                m.getDate(),
                m.getTime(),
                m.getTeam1().getId(),
                m.getTeam1().getName(),
                m.getTeam2().getId(),
                m.getTeam2().getName(),
                m.getScore1(),
                m.getScore2(),
                m.getStatus()
        );
    }
}
