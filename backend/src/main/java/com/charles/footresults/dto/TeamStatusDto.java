package com.charles.footresults.dto;

import com.charles.footresults.domain.TeamCompetitionStatus;

public record TeamStatusDto(
        Long teamId,
        boolean defendingChampion,
        boolean promoted,
        boolean previousCupWinner
) {
    public static TeamStatusDto from(TeamCompetitionStatus s) {
        return new TeamStatusDto(s.getTeam().getId(), s.isDefendingChampion(), s.isPromoted(), s.isPreviousCupWinner());
    }
}
