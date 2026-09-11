package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchCreateDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;

    public MatchService(MatchRepository matchRepository,
                         CompetitionRepository competitionRepository,
                         TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchDto> findByCompetition(Long competitionId) {
        return matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(competitionId)
                .stream().map(MatchDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MatchDto> findByTeam(Long teamId) {
        return matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(teamId, teamId)
                .stream().map(MatchDto::from).toList();
    }

    public MatchDto create(MatchCreateDto dto) {
        Match match = new Match();
        applyFields(match, dto);
        return MatchDto.from(matchRepository.save(match));
    }

    public MatchDto update(Long id, MatchCreateDto dto) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Match introuvable : " + id));
        applyFields(match, dto);
        return MatchDto.from(matchRepository.save(match));
    }

    public void delete(Long id) {
        matchRepository.deleteById(id);
    }

    private void applyFields(Match match, MatchCreateDto dto) {
        Competition competition = competitionRepository.findById(dto.competitionId())
                .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + dto.competitionId()));
        Team team1 = teamRepository.findById(dto.team1Id())
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + dto.team1Id()));
        Team team2 = teamRepository.findById(dto.team2Id())
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + dto.team2Id()));

        match.setCompetition(competition);
        match.setRoundLabel(dto.roundLabel());
        match.setDate(dto.date());
        match.setTime(dto.time());
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setScore1(dto.score1());
        match.setScore2(dto.score2());
        match.setStatus(dto.score1() != null && dto.score2() != null
                ? MatchStatus.COMPLETED
                : MatchStatus.SCHEDULED);
    }
}
