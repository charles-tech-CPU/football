package com.charles.footresults.dto;

import com.charles.footresults.domain.CompetitionType;
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
        CompetitionType competitionType,
        String roundLabel,
        LocalDate date,
        LocalTime time,
        Long team1Id,
        String team1Name,
        String team1LogoPath,
        String team1Country,
        Long team2Id,
        String team2Name,
        String team2LogoPath,
        String team2Country,
        Integer score1,
        Integer score2,
        Integer penaltyScore1,
        Integer penaltyScore2,
        MatchStatus status
) {
    public static MatchDto from(Match m) {
        return new MatchDto(
                m.getId(),
                m.getCompetition().getId(),
                m.getCompetition().getCode(),
                m.getCompetition().getName(),
                m.getCompetition().getCountry(),
                m.getCompetition().getType(),
                m.getRoundLabel(),
                m.getDate(),
                m.getTime(),
                m.getTeam1().getId(),
                m.getTeam1().getName(),
                m.getTeam1().getLogoPath(),
                m.getTeam1().getCountry(),
                m.getTeam2().getId(),
                m.getTeam2().getName(),
                m.getTeam2().getLogoPath(),
                m.getTeam2().getCountry(),
                m.getScore1(),
                m.getScore2(),
                m.getPenaltyScore1(),
                m.getPenaltyScore2(),
                m.getStatus()
        );
    }
}
