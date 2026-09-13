package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamStatusDto;
import com.charles.footresults.dto.TeamStatusUpdateDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamStatusService {

    private final TeamCompetitionStatusRepository statusRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;

    public TeamStatusService(TeamCompetitionStatusRepository statusRepository,
                              CompetitionRepository competitionRepository,
                              TeamRepository teamRepository) {
        this.statusRepository = statusRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamStatusDto> findByCompetition(Long competitionId) {
        return statusRepository.findByCompetitionId(competitionId).stream().map(TeamStatusDto::from).toList();
    }

    public TeamStatusDto upsert(Long competitionId, Long teamId, TeamStatusUpdateDto dto) {
        TeamCompetitionStatus status = statusRepository.findByCompetitionIdAndTeamId(competitionId, teamId)
                .orElseGet(() -> {
                    Competition competition = competitionRepository.findById(competitionId)
                            .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + competitionId));
                    Team team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + teamId));
                    return new TeamCompetitionStatus(competition, team);
                });
        status.setDefendingChampion(dto.defendingChampion());
        status.setPromoted(dto.promoted());
        status.setPreviousCupWinner(dto.previousCupWinner());
        status.setPreviousEuropeCompetition(dto.previousEuropeCompetition());
        status.setGroupName(dto.groupName());
        return TeamStatusDto.from(statusRepository.save(status));
    }
}
