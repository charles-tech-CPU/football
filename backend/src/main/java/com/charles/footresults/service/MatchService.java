package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchCreateDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.MatchPageDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;

    public MatchService(
            MatchRepository matchRepository,
            CompetitionRepository competitionRepository,
            TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchDto> findByCompetition(Long competitionId) {
        return matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(competitionId).stream()
                .map(MatchDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatchDto> findByTeam(Long teamId) {
        return matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(teamId, teamId).stream()
                .map(MatchDto::from)
                .toList();
    }

    /** Derniers matchs joues, toutes competitions confondues, du plus recent au plus ancien. */
    @Transactional(readOnly = true)
    public List<MatchDto> findRecentResults(int limit) {
        return matchRepository.findRecentByStatus(MatchStatus.COMPLETED, PageRequest.of(0, limit)).stream()
                .map(MatchDto::from)
                .toList();
    }

    /**
     * Tous les matchs pas encore joues, toutes competitions confondues, du plus proche au plus
     * lointain, page par page et filtres par type ("league"/"cup"/"ldc"/"el"/"ec", tout par defaut)
     * pour eviter de charger les ~8000 matchs a chaque affichage du calendrier.
     */
    @Transactional(readOnly = true)
    public MatchPageDto findUpcoming(int page, int size, String filter) {
        CompetitionType type =
                switch (filter == null ? "all" : filter) {
                    case "league" -> CompetitionType.LEAGUE;
                    case "cup" -> CompetitionType.DOMESTIC_CUP;
                    default -> null;
                };
        String code =
                switch (filter == null ? "all" : filter) {
                    case "ldc" -> "LDC";
                    case "el" -> "EL";
                    case "ec" -> "EC";
                    default -> null;
                };
        Page<Match> result = matchRepository.findUpcomingFiltered(
                List.of(MatchStatus.COMPLETED, MatchStatus.POSTPONED, MatchStatus.SUSPENDED),
                type,
                code,
                PageRequest.of(page, size));
        return new MatchPageDto(result.getContent().stream().map(MatchDto::from).toList(), result.getTotalElements());
    }

    /** Tous les matchs reportes/suspendus, toutes competitions confondues, sans limite. */
    @Transactional(readOnly = true)
    public List<MatchDto> findPostponedOrSuspended() {
        return matchRepository
                .findByStatusInOrderByDateAscTimeAsc(List.of(MatchStatus.POSTPONED, MatchStatus.SUSPENDED))
                .stream()
                .map(MatchDto::from)
                .toList();
    }

    /** Calendrier international (selections nationales, toutes confederations), saison 2026-2027. */
    @Transactional(readOnly = true)
    public List<MatchDto> findInternational() {
        return matchRepository.findByCompetition_TypeOrderByDateAscTimeAsc(CompetitionType.INTERNATIONAL).stream()
                .map(MatchDto::from)
                .toList();
    }

    public MatchDto create(MatchCreateDto dto) {
        Match match = new Match();
        applyFields(match, dto);
        return MatchDto.from(matchRepository.save(match));
    }

    public MatchDto update(Long id, MatchCreateDto dto) {
        Match match = matchRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Match introuvable : " + id));
        applyFields(match, dto);
        return MatchDto.from(matchRepository.save(match));
    }

    public void delete(Long id) {
        matchRepository.deleteById(id);
    }

    private void applyFields(Match match, MatchCreateDto dto) {
        Competition competition = competitionRepository
                .findById(dto.competitionId())
                .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + dto.competitionId()));
        Team team1 = teamRepository
                .findById(dto.team1Id())
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + dto.team1Id()));
        Team team2 = teamRepository
                .findById(dto.team2Id())
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + dto.team2Id()));

        match.setCompetition(competition);
        match.setRoundLabel(dto.roundLabel());
        match.setDate(dto.date());
        match.setTime(dto.time());
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setScore1(dto.score1());
        match.setScore2(dto.score2());
        match.setPenaltyScore1(dto.penaltyScore1());
        match.setPenaltyScore2(dto.penaltyScore2());
        match.setStatus(dto.status() != null ? dto.status() : statusFromScores(dto));
    }

    /** Sans statut force : un match dont les deux scores sont saisis est termine, sinon a venir. */
    private static MatchStatus statusFromScores(MatchCreateDto dto) {
        return dto.score1() != null && dto.score2() != null ? MatchStatus.COMPLETED : MatchStatus.SCHEDULED;
    }
}
