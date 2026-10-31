package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.AllLeaguesStatsDto;
import com.charles.footresults.dto.AllLeaguesStatsDto.LeagueSummary;
import com.charles.footresults.dto.CompetitionStatsDto;
import com.charles.footresults.dto.CompetitionStatsDto.TeamValue;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Statistiques d'une competition : rien n'est stocke, tout est recalcule a partir de ses matchs
 * joues (2 scores saisis, cf. TeamProfileService.isPlayed), dans l'ordre chronologique. Les tirs
 * au but ne changent pas le resultat (match nul). Chaque classement est limite a TOP_SIZE lignes ;
 * a egalite, l'equipe qui a joue le moins de matchs passe devant pour les attaques, celle qui en
 * a joue le plus pour les defenses.
 */
@Service
@Transactional(readOnly = true)
public class StatsService {

    static final int TOP_SIZE = 5;

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;

    public StatsService(CompetitionRepository competitionRepository, MatchRepository matchRepository) {
        this.competitionRepository = competitionRepository;
        this.matchRepository = matchRepository;
    }

    public CompetitionStatsDto competitionStats(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new EntityNotFoundException("Competition introuvable : " + competitionId);
        }
        // Le depot trie par date puis heure ; les matchs sans date passent avant (tours sans date
        // exploitable, toujours joues avant les autres).
        List<Match> played = matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(competitionId).stream()
                .filter(TeamProfileService::isPlayed)
                .toList();
        return statsOf(played);
    }

    /**
     * Tous les championnats de la saison en cours reunis (memes classements que pour une
     * competition, toutes equipes confondues), plus le bilan de chaque championnat pour les comparer.
     * countries vide = tous les pays ; sinon seulement ces championnats (ex : le top 5 UEFA).
     */
    public AllLeaguesStatsDto allLeaguesStats(Set<String> countries) {
        // Saison en cours = celle des coupes d'Europe : un championnat de la saison suivante deja
        // prepare (cf. NextSeasonService) n'efface pas les autres avant le debut de cette saison.
        int season = competitionRepository.findByType(CompetitionType.CONTINENTAL_CUP).stream()
                .mapToInt(Competition::getSeason)
                .max()
                .orElseGet(() -> competitionRepository.findByType(CompetitionType.LEAGUE).stream()
                        .mapToInt(Competition::getSeason)
                        .max()
                        .orElse(0));
        List<Competition> leagues = competitionRepository.findByType(CompetitionType.LEAGUE).stream()
                .filter(c -> c.getSeason() == season)
                .filter(c -> countries.isEmpty() || countries.contains(c.getCountry()))
                .toList();
        Set<Long> leagueIds = leagues.stream().map(Competition::getId).collect(Collectors.toSet());
        List<Match> played = matchRepository
                .findByCompetition_TypeAndCompetition_SeasonOrderByDateAscTimeAsc(CompetitionType.LEAGUE, season)
                .stream()
                .filter(TeamProfileService::isPlayed)
                .filter(m -> leagueIds.contains(m.getCompetition().getId()))
                .toList();

        Map<Long, List<Match>> byLeague = new LinkedHashMap<>();
        for (Match m : played) {
            byLeague.computeIfAbsent(m.getCompetition().getId(), id -> new ArrayList<>())
                    .add(m);
        }
        List<LeagueSummary> summaries = leagues.stream()
                .map(c -> summaryOf(c, byLeague.getOrDefault(c.getId(), List.of())))
                .sorted(Comparator.comparing(LeagueSummary::country, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return new AllLeaguesStatsDto(statsOf(played), summaries);
    }

    private static LeagueSummary summaryOf(Competition league, List<Match> played) {
        int goals = 0;
        int homeWins = 0;
        int draws = 0;
        for (Match m : played) {
            goals += m.getScore1() + m.getScore2();
            if (m.getScore1() > m.getScore2()) homeWins++;
            else if (m.getScore1().equals(m.getScore2())) draws++;
        }
        return new LeagueSummary(
                league.getId(),
                league.getName(),
                league.getCountry(),
                played.size(),
                goals,
                homeWins,
                draws,
                played.size() - homeWins - draws);
    }

    private static CompetitionStatsDto statsOf(List<Match> played) {
        int goals = 0;
        int homeWins = 0;
        int draws = 0;
        int awayWins = 0;
        Map<Long, Tally> tallies = new LinkedHashMap<>();
        for (Match m : played) {
            goals += m.getScore1() + m.getScore2();
            if (m.getScore1() > m.getScore2()) homeWins++;
            else if (m.getScore1().equals(m.getScore2())) draws++;
            else awayWins++;
            tallies.computeIfAbsent(m.getTeam1().getId(), id -> new Tally(m.getTeam1()))
                    .add(m.getScore1(), m.getScore2());
            tallies.computeIfAbsent(m.getTeam2().getId(), id -> new Tally(m.getTeam2()))
                    .add(m.getScore2(), m.getScore1());
        }
        List<Tally> teams = new ArrayList<>(tallies.values());

        return new CompetitionStatsDto(
                played.size(),
                goals,
                homeWins,
                draws,
                awayWins,
                top(
                        teams,
                        t -> t.goalsFor,
                        Comparator.comparingInt((Tally t) -> -t.goalsFor).thenComparingInt(t -> t.played)),
                top(
                        teams,
                        t -> t.goalsAgainst,
                        Comparator.comparingInt((Tally t) -> t.goalsAgainst).thenComparingInt(t -> -t.played)),
                topMatches(
                        played,
                        Comparator.comparingInt((Match m) -> -Math.abs(m.getScore1() - m.getScore2()))
                                .thenComparingInt(m -> -(m.getScore1() + m.getScore2()))),
                topMatches(
                        played,
                        Comparator.comparingInt((Match m) -> -(m.getScore1() + m.getScore2()))
                                .thenComparingInt(m -> -Math.abs(m.getScore1() - m.getScore2()))),
                topRuns(teams, t -> t.currentRun(r -> r != 'L')),
                topRuns(teams, t -> t.currentRun(r -> r == 'W')),
                topRuns(teams, t -> t.currentRun(r -> r != 'W')));
    }

    private static List<TeamValue> top(List<Tally> teams, ToIntFunction<Tally> value, Comparator<Tally> order) {
        return teams.stream()
                .sorted(order)
                .limit(TOP_SIZE)
                .map(t -> t.toValue(value.applyAsInt(t)))
                .toList();
    }

    private static List<MatchDto> topMatches(List<Match> played, Comparator<Match> order) {
        return played.stream().sorted(order).limit(TOP_SIZE).map(MatchDto::from).toList();
    }

    /** Series en cours de longueur >= 1 uniquement, de la plus longue a la plus courte. */
    private static List<TeamValue> topRuns(List<Tally> teams, ToIntFunction<Tally> run) {
        return teams.stream()
                .filter(t -> run.applyAsInt(t) > 0)
                .sorted(Comparator.comparingInt((Tally t) -> -run.applyAsInt(t)))
                .limit(TOP_SIZE)
                .map(t -> t.toValue(run.applyAsInt(t)))
                .toList();
    }

    private static final class Tally {
        private final Team team;
        private int played;
        private int goalsFor;
        private int goalsAgainst;
        /** Resultats dans l'ordre chronologique : 'W', 'D' ou 'L'. */
        private final StringBuilder results = new StringBuilder();

        Tally(Team team) {
            this.team = team;
        }

        void add(int scored, int conceded) {
            played++;
            goalsFor += scored;
            goalsAgainst += conceded;
            results.append(result(scored, conceded));
        }

        private static char result(int scored, int conceded) {
            if (scored > conceded) return 'W';
            return scored == conceded ? 'D' : 'L';
        }

        /** Nombre de derniers matchs consecutifs dont le resultat verifie la condition. */
        int currentRun(IntPredicate matches) {
            int run = 0;
            for (int i = results.length() - 1; i >= 0 && matches.test(results.charAt(i)); i--) run++;
            return run;
        }

        TeamValue toValue(int value) {
            return new TeamValue(team.getId(), team.getName(), team.getLogoPath(), played, value);
        }
    }
}
