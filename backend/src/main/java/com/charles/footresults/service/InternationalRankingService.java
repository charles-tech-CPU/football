package com.charles.footresults.service;

import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.InternationalRankingRowDto;
import com.charles.footresults.repository.MatchRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classement mondial des selections : un Elo recalcule a la volee a partir de tous les matchs
 * internationaux joues, dans l'ordre chronologique (historique V116-V123 + saison en cours ; un
 * resultat saisi compte des la requete suivante). Inspire du World Football Elo Ratings
 * (eloratings.net) et du power ranking du projet LoL :
 *
 * <ul>
 *   <li>Note de depart selon la confederation ({@link #START_RATINGS}) : les confederations ne se
 *       rencontrent qu'en phase finale de Coupe du monde, en barrages intercontinentaux et via les
 *       invites (Copa America, Gold Cup), la note de depart fixe donc leur niveau relatif initial.
 *   <li>Qualite des adversaires : une victoire rapporte d'autant plus que l'adversaire etait bien
 *       note au moment du match, une defaite contre plus faible coute cher.
 *   <li>Importance du match ({@link #k}) : phase finale de Coupe du monde 60, phase finale
 *       continentale 50, qualifications et Ligues des Nations 40.
 *   <li>Ecart de buts : x1,5 a 2 buts d'ecart, x1,75 a 3, puis +1/8 par but supplementaire.
 *   <li>Avantage du terrain : +100 points pour l'equipe qui recoit, sauf terrain neutre.
 *   <li>Forme recente : chaque match deplace la note courante, les derniers resultats pesent donc
 *       naturellement le plus.
 * </ul>
 * Une seance de tirs au but compte comme un match nul (comme eloratings.net).
 */
@Service
@Transactional(readOnly = true)
public class InternationalRankingService {

    /** Note de depart par confederation (prefixe du code des competitions, cf. utils/confederations.js). */
    static final Map<String, Integer> START_RATINGS = Map.of(
            "CONMEBOL", 1700,
            "UEFA", 1650,
            "CONCACAF", 1450,
            "CAF", 1450,
            "AFC", 1400,
            "OFC", 1250);

    static final int DEFAULT_START_RATING = 1400;
    static final double HOME_ADVANTAGE = 100;
    static final int FORM_LENGTH = 5;
    static final int TREND_DAYS = 90;

    private static final List<String> CONTINENTAL_FINALS =
            List.of("UEFA_EURO_", "CONMEBOL_COPA_", "CAF_AFCON_", "AFC_ASIANCUP_", "CONCACAF_GOLDCUP_");

    static final Comparator<Match> CHRONOLOGICAL = Comparator.comparing(Match::getDate)
            .thenComparing(Match::getTime, Comparator.nullsFirst(Comparator.<LocalTime>naturalOrder()))
            .thenComparing(Match::getId, Comparator.nullsLast(Comparator.<Long>naturalOrder()));

    private final MatchRepository matchRepository;

    public InternationalRankingService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<InternationalRankingRowDto> compute() {
        List<Match> played =
                matchRepository
                        .findByCompetition_TypeAndStatus(CompetitionType.INTERNATIONAL, MatchStatus.COMPLETED)
                        .stream()
                        .filter(InternationalRankingService::isRated)
                        .sorted(CHRONOLOGICAL)
                        .toList();
        if (played.isEmpty()) {
            return List.of();
        }
        Map<Long, String> confederations = confederations(played);
        LocalDate trendCutoff = played.get(played.size() - 1).getDate().minusDays(TREND_DAYS);
        Map<Long, Integer> previousRanks = ranks(rate(
                played.stream().filter(m -> !m.getDate().isAfter(trendCutoff)).toList(), confederations));

        List<TeamRating> ordered = ordered(rate(played, confederations));
        List<InternationalRankingRowDto> rows = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            TeamRating r = ordered.get(i);
            int rank = i + 1;
            Integer previous = previousRanks.get(r.team.getId());
            rows.add(new InternationalRankingRowDto(
                    rank,
                    previous != null ? previous - rank : null,
                    r.team.getId(),
                    r.team.getName(),
                    r.team.getCountry(),
                    confederations.get(r.team.getId()),
                    (int) Math.round(r.initial),
                    (int) Math.round(r.rating),
                    (int) Math.round(r.rating - r.ratingAt(trendCutoff)),
                    r.played,
                    r.wins,
                    r.draws,
                    r.losses,
                    r.goalsFor,
                    r.goalsAgainst,
                    r.form.subList(Math.max(0, r.form.size() - FORM_LENGTH), r.form.size()),
                    r.bestWin,
                    r.lastPlayed));
        }
        return rows;
    }

    private static boolean isRated(Match m) {
        return m.getDate() != null && m.getScore1() != null && m.getScore2() != null;
    }

    /** Confederation (prefixe du code) des competitions continentales ou l'equipe a joue le plus. */
    static Map<Long, String> confederations(List<Match> matches) {
        Map<Long, Map<String, Integer>> counts = new HashMap<>();
        for (Match m : matches) {
            String code = m.getCompetition().getCode();
            int sep = code.indexOf('_');
            String prefix = sep > 0 ? code.substring(0, sep) : code;
            if (!START_RATINGS.containsKey(prefix)) {
                continue;
            }
            for (Team t : List.of(m.getTeam1(), m.getTeam2())) {
                counts.computeIfAbsent(t.getId(), id -> new HashMap<>()).merge(prefix, 1, Integer::sum);
            }
        }
        Map<Long, String> result = new HashMap<>();
        counts.forEach((teamId, byConf) -> result.put(
                teamId,
                byConf.entrySet().stream()
                        .max(Map.Entry.<String, Integer>comparingByValue()
                                .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                        .orElseThrow()
                        .getKey()));
        return result;
    }

    static int startRating(String confederation) {
        return confederation == null
                ? DEFAULT_START_RATING
                : START_RATINGS.getOrDefault(confederation, DEFAULT_START_RATING);
    }

    /** Importance du match : phase finale de Coupe du monde, phase finale continentale, le reste. */
    static double k(Match m) {
        String code = m.getCompetition().getCode();
        if (code.startsWith("FIFA_WC_")) {
            return 60;
        }
        return CONTINENTAL_FINALS.stream().anyMatch(code::startsWith) ? 50 : 40;
    }

    /** Multiplicateur d'ecart de buts (eloratings.net). */
    static double goalMultiplier(int goalDiff) {
        if (goalDiff <= 1) {
            return 1;
        }
        if (goalDiff == 2) {
            return 1.5;
        }
        return (11.0 + goalDiff) / 8;
    }

    private static Map<Long, TeamRating> rate(List<Match> matches, Map<Long, String> confederations) {
        Map<Long, TeamRating> ratings = new LinkedHashMap<>();
        for (Match m : matches) {
            TeamRating a = ratings.computeIfAbsent(
                    m.getTeam1().getId(), id -> new TeamRating(m.getTeam1(), startRating(confederations.get(id))));
            TeamRating b = ratings.computeIfAbsent(
                    m.getTeam2().getId(), id -> new TeamRating(m.getTeam2(), startRating(confederations.get(id))));
            int s1 = m.getScore1();
            int s2 = m.getScore2();
            double home = m.isNeutral() ? 0 : HOME_ADVANTAGE;
            double expectedA = 1 / (1 + Math.pow(10, (b.rating - a.rating - home) / 400));
            double resultA = s1 == s2 ? 0.5 : (s1 > s2 ? 1 : 0);
            double delta = k(m) * goalMultiplier(Math.abs(s1 - s2)) * (resultA - expectedA);
            int ratingA = (int) Math.round(a.rating);
            int ratingB = (int) Math.round(b.rating);
            a.add(m, s1, s2, delta, b.team, ratingB);
            b.add(m, s2, s1, -delta, a.team, ratingA);
        }
        return ratings;
    }

    /** Meilleure note d'abord ; a egalite, le plus de victoires puis le nom. */
    private static List<TeamRating> ordered(Map<Long, TeamRating> ratings) {
        return ratings.values().stream()
                .sorted(Comparator.comparingDouble((TeamRating r) -> r.rating)
                        .reversed()
                        .thenComparing(Comparator.comparingInt((TeamRating r) -> r.wins)
                                .reversed())
                        .thenComparing(r -> r.team.getName()))
                .toList();
    }

    private static Map<Long, Integer> ranks(Map<Long, TeamRating> ratings) {
        Map<Long, Integer> result = new HashMap<>();
        List<TeamRating> ordered = ordered(ratings);
        for (int i = 0; i < ordered.size(); i++) {
            result.put(ordered.get(i).team.getId(), i + 1);
        }
        return result;
    }

    /** Note courante d'une selection et ce qu'on en affiche. */
    private static final class TeamRating {
        final Team team;
        final double initial;
        double rating;
        int played;
        int wins;
        int draws;
        int losses;
        int goalsFor;
        int goalsAgainst;
        final List<String> form = new ArrayList<>();
        InternationalRankingRowDto.BestWin bestWin;
        LocalDate lastPlayed;
        /** Note apres chaque match, pour l'evolution sur les derniers jours. */
        final List<Map.Entry<LocalDate, Double>> history = new ArrayList<>();

        TeamRating(Team team, int initial) {
            this.team = team;
            this.initial = initial;
            this.rating = initial;
        }

        void add(Match m, int own, int other, double delta, Team opponent, int opponentRating) {
            rating += delta;
            played++;
            goalsFor += own;
            goalsAgainst += other;
            if (own > other) {
                wins++;
                form.add("W");
                if (bestWin == null || opponentRating > bestWin.opponentRating()) {
                    bestWin = new InternationalRankingRowDto.BestWin(
                            m.getId(),
                            opponent.getId(),
                            opponent.getName(),
                            opponentRating,
                            own + "-" + other,
                            m.getCompetition().getName());
                }
            } else if (own < other) {
                losses++;
                form.add("L");
            } else {
                draws++;
                form.add("D");
            }
            lastPlayed = m.getDate();
            history.add(Map.entry(m.getDate(), rating));
        }

        /** Note a la fin du jour donne (note de depart si l'equipe n'avait pas encore joue). */
        double ratingAt(LocalDate day) {
            double result = initial;
            for (Map.Entry<LocalDate, Double> e : history) {
                if (e.getKey().isAfter(day)) {
                    break;
                }
                result = e.getValue();
            }
            return result;
        }
    }
}
