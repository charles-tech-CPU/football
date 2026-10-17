package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.dto.TeamHeadToHeadDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tete-a-tete entre deux equipes : bilan de toutes leurs confrontations, toutes competitions et
 * saisons confondues, deduit des matchs (jamais stocke). Meme definition d'un match joue que la
 * fiche club (cf. TeamProfileService) : ses deux scores sont saisis.
 */
@Service
@Transactional(readOnly = true)
public class TeamHeadToHeadService {

    private static final Comparator<Match> CHRONOLOGICAL = Comparator.comparing(
                    Match::getDate, Comparator.nullsFirst(Comparator.<LocalDate>naturalOrder()))
            .thenComparing(Match::getTime, Comparator.nullsFirst(Comparator.<LocalTime>naturalOrder()))
            .thenComparing(Match::getId);

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public TeamHeadToHeadService(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public TeamHeadToHeadDto headToHead(Long teamId, Long opponentId) {
        if (teamId.equals(opponentId)) {
            throw new IllegalArgumentException("Choisir deux equipes differentes");
        }
        Team team = find(teamId);
        Team opponent = find(opponentId);
        List<Match> matches = matchRepository.findBetween(teamId, opponentId);

        List<Match> played = matches.stream()
                .filter(TeamProfileService::isPlayed)
                .sorted(CHRONOLOGICAL.reversed())
                .toList();
        List<Match> upcoming = matches.stream()
                .filter(m -> !TeamProfileService.isPlayed(m))
                .sorted(Comparator.comparing(Match::getDate, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
                        .thenComparing(Match::getTime, Comparator.nullsLast(Comparator.<LocalTime>naturalOrder()))
                        .thenComparing(Match::getId))
                .toList();

        Tally total = new Tally();
        Map<Long, Tally> byCompetition = new LinkedHashMap<>();
        Map<Long, Competition> competitions = new LinkedHashMap<>();
        Match biggestWin = null;
        Match biggestLoss = null;
        for (Match m : played) {
            int own = goals(m, teamId, true);
            int other = goals(m, teamId, false);
            total.add(own, other);
            competitions.putIfAbsent(m.getCompetition().getId(), m.getCompetition());
            byCompetition
                    .computeIfAbsent(m.getCompetition().getId(), id -> new Tally())
                    .add(own, other);
            if (own == other) {
                countShootout(m, teamId, total);
            } else if (own > other && isBigger(m, biggestWin, teamId, true)) {
                biggestWin = m;
            } else if (own < other && isBigger(m, biggestLoss, teamId, false)) {
                biggestLoss = m;
            }
        }

        List<TeamHeadToHeadDto.CompetitionRecord> records = new ArrayList<>();
        byCompetition.forEach((id, t) -> {
            Competition c = competitions.get(id);
            records.add(new TeamHeadToHeadDto.CompetitionRecord(
                    id, c.getName(), c.getType(), c.getCountry(), t.played, t.wins, t.draws, t.losses));
        });

        return new TeamHeadToHeadDto(
                TeamDto.from(team),
                TeamDto.from(opponent),
                total.played,
                total.wins,
                total.draws,
                total.losses,
                total.goalsFor,
                total.goalsAgainst,
                total.penaltyWins,
                total.penaltyLosses,
                biggestWin != null ? MatchDto.from(biggestWin) : null,
                biggestLoss != null ? MatchDto.from(biggestLoss) : null,
                records,
                played.stream().map(MatchDto::from).toList(),
                upcoming.stream().map(MatchDto::from).toList());
    }

    private Team find(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
    }

    /** Buts de l'equipe donnee (own) ou de son adversaire (!own) dans un match joue. */
    private static int goals(Match m, Long teamId, boolean own) {
        boolean home = m.getTeam1().getId().equals(teamId);
        return home == own ? m.getScore1() : m.getScore2();
    }

    private static void countShootout(Match m, Long teamId, Tally total) {
        if (m.getPenaltyScore1() == null || m.getPenaltyScore2() == null) {
            return;
        }
        boolean home = m.getTeam1().getId().equals(teamId);
        int own = home ? m.getPenaltyScore1() : m.getPenaltyScore2();
        int other = home ? m.getPenaltyScore2() : m.getPenaltyScore1();
        if (own > other) total.penaltyWins++;
        else if (own < other) total.penaltyLosses++;
    }

    /**
     * m est-il un plus large succes que current, pour le vainqueur (ownWins = l'equipe donnee) ?
     * Plus grand ecart d'abord, puis le plus de buts du vainqueur ; a egalite on garde current,
     * le plus recent (matchs parcourus du plus recent au plus ancien).
     */
    private static boolean isBigger(Match m, Match current, Long teamId, boolean ownWins) {
        if (current == null) {
            return true;
        }
        int margin = Math.abs(m.getScore1() - m.getScore2());
        int currentMargin = Math.abs(current.getScore1() - current.getScore2());
        if (margin != currentMargin) {
            return margin > currentMargin;
        }
        return goals(m, teamId, ownWins) > goals(current, teamId, ownWins);
    }

    private static final class Tally {
        int played;
        int wins;
        int draws;
        int losses;
        int goalsFor;
        int goalsAgainst;
        int penaltyWins;
        int penaltyLosses;

        void add(int own, int other) {
            played++;
            goalsFor += own;
            goalsAgainst += other;
            if (own > other) wins++;
            else if (own == other) draws++;
            else losses++;
        }
    }
}
