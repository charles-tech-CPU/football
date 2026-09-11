package com.charles.footresults.service;

import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.HeadToHeadCellDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classement et tete-a-tete ne sont jamais stockes : ils sont recalcules a
 * partir des matchs COMPLETED d'une competition. Points classiques : victoire
 * = 3, nul = 1, defaite = 0. N'a de sens que pour une competition de type
 * LEAGUE (les coupes sont a elimination directe, pas de classement).
 */
@Service
public class StandingsService {

    private static final int POINTS_WIN = 3;
    private static final int POINTS_DRAW = 1;

    private final MatchRepository matchRepository;

    public StandingsService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<StandingRowDto> computeStandings(Long competitionId) {
        Map<Long, TeamTally> byTeam = new LinkedHashMap<>();

        for (Match m : playedMatches(competitionId)) {
            tallyFor(byTeam, m.getTeam1()).addResult(m.getScore1(), m.getScore2());
            tallyFor(byTeam, m.getTeam2()).addResult(m.getScore2(), m.getScore1());
        }

        return byTeam.values().stream()
                .map(TeamTally::toDto)
                .sorted(Comparator
                        .comparingInt(StandingRowDto::points).reversed()
                        .thenComparing(Comparator.comparingInt(StandingRowDto::goalDifference).reversed())
                        .thenComparing(Comparator.comparingInt(StandingRowDto::goalsFor).reversed()))
                .toList();
    }

    public List<HeadToHeadCellDto> computeHeadToHead(Long competitionId) {
        Map<Long, Map<Long, int[]>> tally = new LinkedHashMap<>();

        for (Match m : playedMatches(competitionId)) {
            addH2H(tally, m.getTeam1().getId(), m.getTeam2().getId(), m.getScore1(), m.getScore2());
            addH2H(tally, m.getTeam2().getId(), m.getTeam1().getId(), m.getScore2(), m.getScore1());
        }

        List<HeadToHeadCellDto> result = new ArrayList<>();
        tally.forEach((teamAId, opponents) -> opponents.forEach((teamBId, wdl) ->
                result.add(new HeadToHeadCellDto(teamAId, teamBId, wdl[0], wdl[1], wdl[2]))));
        return result;
    }

    private List<Match> playedMatches(Long competitionId) {
        return matchRepository.findByCompetitionIdAndStatusOrderByDateAscTimeAsc(competitionId, MatchStatus.COMPLETED);
    }

    private TeamTally tallyFor(Map<Long, TeamTally> byTeam, Team team) {
        return byTeam.computeIfAbsent(team.getId(), id -> new TeamTally(team));
    }

    private void addH2H(Map<Long, Map<Long, int[]>> tally, Long teamAId, Long teamBId, Integer goalsA, Integer goalsB) {
        if (goalsA == null || goalsB == null) {
            return;
        }
        int[] wdl = tally.computeIfAbsent(teamAId, id -> new LinkedHashMap<>())
                .computeIfAbsent(teamBId, id -> new int[3]);
        if (goalsA > goalsB) {
            wdl[0]++;
        } else if (goalsA.equals(goalsB)) {
            wdl[1]++;
        } else {
            wdl[2]++;
        }
    }

    /** Petit accumulateur mutable, interne au calcul, jamais expose ni persiste. */
    private static final class TeamTally {
        private final Team team;
        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;

        private TeamTally(Team team) {
            this.team = team;
        }

        private void addResult(Integer goalsFor, Integer goalsAgainst) {
            if (goalsFor == null || goalsAgainst == null) {
                return;
            }
            played++;
            this.goalsFor += goalsFor;
            this.goalsAgainst += goalsAgainst;
            if (goalsFor > goalsAgainst) {
                won++;
            } else if (goalsFor.equals(goalsAgainst)) {
                drawn++;
            } else {
                lost++;
            }
        }

        private StandingRowDto toDto() {
            int points = won * POINTS_WIN + drawn * POINTS_DRAW;
            return new StandingRowDto(team.getId(), team.getName(), played, won, drawn, lost,
                    goalsFor, goalsAgainst, goalsFor - goalsAgainst, points);
        }
    }
}
