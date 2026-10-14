package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.dto.TeamProfileDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fiche club : rien n'est stocke, tout est deduit des matchs du club (toutes competitions) et
 * des classements deja calcules (championnat via StandingsService, UEFA via UefaRankingService).
 * Un match "joue" a ses 2 scores saisis (quel que soit son statut, forfait compris) ; les tirs
 * au but ne changent pas le resultat (match nul).
 */
@Service
@Transactional(readOnly = true)
public class TeamProfileService {

    static final int FORM_SIZE = 5;
    static final int RECENT_SIZE = 10;
    static final int UPCOMING_SIZE = 5;

    private static final Comparator<Match> CHRONOLOGICAL = Comparator.comparing(
                    Match::getDate, Comparator.nullsFirst(Comparator.<LocalDate>naturalOrder()))
            .thenComparing(Match::getTime, Comparator.nullsFirst(Comparator.<LocalTime>naturalOrder()))
            .thenComparing(Match::getId);

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StandingsService standingsService;
    private final UefaRankingService uefaRankingService;

    public TeamProfileService(
            TeamRepository teamRepository,
            MatchRepository matchRepository,
            StandingsService standingsService,
            UefaRankingService uefaRankingService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.standingsService = standingsService;
        this.uefaRankingService = uefaRankingService;
    }

    public TeamProfileDto profile(Long teamId) {
        Team team = teamRepository
                .findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + teamId));
        List<Match> matches = matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(teamId, teamId);

        // Joues : du plus recent au plus ancien ; a venir : du plus proche au plus lointain
        // (sans date en dernier).
        List<Match> played = matches.stream()
                .filter(TeamProfileService::isPlayed)
                .sorted(CHRONOLOGICAL.reversed())
                .toList();
        List<Match> upcoming = matches.stream()
                .filter(m -> !isPlayed(m))
                .sorted(Comparator.comparing(Match::getDate, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
                        .thenComparing(Match::getTime, Comparator.nullsLast(Comparator.<LocalTime>naturalOrder()))
                        .thenComparing(Match::getId))
                .limit(UPCOMING_SIZE)
                .toList();

        List<String> results = played.stream().map(m -> resultFor(m, teamId)).toList();
        return new TeamProfileDto(
                TeamDto.from(team),
                leagueStanding(matches, teamId),
                uefaStanding(teamId),
                results.stream().limit(FORM_SIZE).toList(),
                streak(results),
                played.stream().limit(RECENT_SIZE).map(MatchDto::from).toList(),
                upcoming.stream().map(MatchDto::from).toList());
    }

    static boolean isPlayed(Match m) {
        return m.getScore1() != null && m.getScore2() != null;
    }

    /** "W", "D" ou "L" du point de vue de l'equipe donnee (match joue). */
    static String resultFor(Match m, Long teamId) {
        boolean home = m.getTeam1().getId().equals(teamId);
        int own = home ? m.getScore1() : m.getScore2();
        int other = home ? m.getScore2() : m.getScore1();
        if (own > other) return "W";
        return own == other ? "D" : "L";
    }

    /** Resultats du plus recent au plus ancien. */
    static TeamProfileDto.Streak streak(List<String> results) {
        if (results.isEmpty()) {
            return null;
        }
        String last = results.get(0);
        int length = 0;
        while (length < results.size() && results.get(length).equals(last)) length++;
        int unbeaten = 0;
        while (unbeaten < results.size() && !results.get(unbeaten).equals("L")) unbeaten++;
        return new TeamProfileDto.Streak(last, length, unbeaten);
    }

    private TeamProfileDto.LeagueStanding leagueStanding(List<Match> matches, Long teamId) {
        Competition league = matches.stream()
                .map(Match::getCompetition)
                .filter(c -> c.getType() == CompetitionType.LEAGUE)
                .max(Comparator.comparing(Competition::getSeason, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
        if (league == null) {
            return null;
        }
        List<StandingRowDto> rows = standingsService.computeStandings(league.getId());
        for (int i = 0; i < rows.size(); i++) {
            StandingRowDto row = rows.get(i);
            if (row.teamId().equals(teamId)) {
                return new TeamProfileDto.LeagueStanding(
                        league.getId(),
                        league.getName(),
                        i + 1,
                        rows.size(),
                        row.played(),
                        row.points(),
                        row.goalsFor(),
                        row.goalsAgainst());
            }
        }
        return new TeamProfileDto.LeagueStanding(league.getId(), league.getName(), null, rows.size(), 0, 0, 0, 0);
    }

    private TeamProfileDto.UefaStanding uefaStanding(Long teamId) {
        return uefaRankingService.findClubRankings().stream()
                .filter(r -> teamId.equals(r.teamId()))
                .findFirst()
                .map(r -> new TeamProfileDto.UefaStanding(
                        r.id(), r.rank(), r.startRank(), r.total(), r.points2027(), r.currentCup()))
                .orElse(null);
    }
}
