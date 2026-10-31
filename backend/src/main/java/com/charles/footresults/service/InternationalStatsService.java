package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.InternationalStatsDto;
import com.charles.footresults.dto.InternationalStatsDto.Journey;
import com.charles.footresults.dto.InternationalStatsDto.TeamRecord;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bilan historique d'une competition de selections, toutes editions confondues (memes familles que
 * utils/internationalEditions.js cote interface) : bilan de chaque selection en phase finale et en
 * qualifications, et parcours en phase finale.
 *
 * Stade atteint dans une edition = le tour le plus avance joue (finale, demi-finales ou match pour
 * la 3e place, quarts, 8es, 16es) ; une 2e phase de groupes (Coupe du monde 1974-1982, tour final de
 * la Copa America...) compte selon son nombre d'equipes (4 : demi-finale, 12 au plus : quarts, 16
 * au plus : 8es). Vainqueur = gagnant de la finale (match rejoue, aller-retour et tirs au but
 * compris) ou, sans finale, premier de la poule finale unique (Coupe du monde 1950, anciennes Copa
 * America), a 2 points la victoire avant 1995 comme a l'epoque.
 */
@Service
@Transactional(readOnly = true)
public class InternationalStatsService {

    static final int GROUP = 0;
    static final int ROUND_OF_32 = 1;
    static final int ROUND_OF_16 = 2;
    static final int QUARTER = 3;
    static final int SEMI = 4;
    static final int RUNNER_UP = 5;
    static final int WINNER = 6;

    private record Family(String key, Pattern finals, Pattern qualifiers) {
        Family(String key, String finals, String qualifiers) {
            this(key, Pattern.compile(finals), qualifiers == null ? null : Pattern.compile(qualifiers));
        }
    }

    private static final List<Family> FAMILIES = List.of(
            new Family("WC", "^FIFA_WC_\\d{4}$", "^[A-Z]+_WCQ_\\d{4}$"),
            new Family("EURO", "^UEFA_EURO_\\d{4}$", "^UEFA_EUROQ_\\d{4}$"),
            new Family("COPA", "^CONMEBOL_COPA_\\d{4}B?$", null),
            new Family("CAN", "^CAF_AFCON_\\d{4}$", "^(CAF_AFCONQ|CAF_CAN)_\\d{4}$"),
            new Family("ASIE", "^AFC_ASIANCUP_\\d{4}$", "^AFC_ASIANCUPQ_\\d{4}$"),
            new Family("GOLD", "^CONCACAF_GOLDCUP_\\d{4}$", "^CONCACAF_GOLDCUPQ_\\d{4}$"),
            new Family("UNL", "^UEFA_NL_\\d{4}$", null),
            new Family("CNL", "^CONCACAF_NL_\\d{4}$", null));

    private static final Comparator<Match> CHRONOLOGICAL = Comparator.comparing(
                    Match::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Match::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;

    public InternationalStatsService(CompetitionRepository competitionRepository, MatchRepository matchRepository) {
        this.competitionRepository = competitionRepository;
        this.matchRepository = matchRepository;
    }

    public InternationalStatsDto stats(String familyKey) {
        Family family = FAMILIES.stream()
                .filter(f -> f.key().equals(familyKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Competition de selections inconnue : " + familyKey));
        List<Competition> finals = new ArrayList<>();
        List<Competition> qualifiers = new ArrayList<>();
        for (Competition c : competitionRepository.findByType(CompetitionType.INTERNATIONAL)) {
            if (family.finals().matcher(c.getCode()).matches()) {
                finals.add(c);
            } else if (family.qualifiers() != null
                    && family.qualifiers().matcher(c.getCode()).matches()) {
                qualifiers.add(c);
            }
        }
        List<String> codes = new ArrayList<>();
        finals.forEach(c -> codes.add(c.getCode()));
        qualifiers.forEach(c -> codes.add(c.getCode()));
        Map<String, List<Match>> byCode = codes.isEmpty()
                ? Map.of()
                : matchRepository.findByCompetition_CodeIn(codes).stream()
                        .filter(m -> !isPlaceholder(m.getTeam1()) && !isPlaceholder(m.getTeam2()))
                        .sorted(CHRONOLOGICAL)
                        .collect(Collectors.groupingBy(m -> m.getCompetition().getCode()));

        Map<Long, JourneyTally> journeys = new HashMap<>();
        for (Competition c : finals) {
            List<Match> matches = byCode.getOrDefault(c.getCode(), List.of());
            editionStages(matches, editionYear(c.getCode()))
                    .forEach((team, stage) -> journeys.computeIfAbsent(team.getId(), id -> new JourneyTally(team))
                            .add(stage, edition(c.getCode())));
        }
        return new InternationalStatsDto(
                familyKey,
                records(finals, byCode),
                records(qualifiers, byCode),
                journeys.values().stream()
                        .map(JourneyTally::toDto)
                        .sorted(Comparator.comparingInt(Journey::titles)
                                .thenComparingInt(Journey::runnerUps)
                                .thenComparingInt(Journey::semiFinals)
                                .thenComparingInt(Journey::quarterFinals)
                                .thenComparingInt(Journey::roundOf16)
                                .thenComparingInt(Journey::roundOf32)
                                .thenComparingInt(Journey::editions)
                                .reversed()
                                .thenComparing(Journey::teamName))
                        .toList());
    }

    /** Bilan de chaque selection sur ces competitions ; editions = editions (annees) jouees. */
    private static List<TeamRecord> records(List<Competition> competitions, Map<String, List<Match>> byCode) {
        Map<Long, RecordTally> tallies = new HashMap<>();
        for (Competition c : competitions) {
            String edition = edition(c.getCode());
            for (Match m : byCode.getOrDefault(c.getCode(), List.of())) {
                RecordTally t1 = tallies.computeIfAbsent(m.getTeam1().getId(), id -> new RecordTally(m.getTeam1()));
                RecordTally t2 = tallies.computeIfAbsent(m.getTeam2().getId(), id -> new RecordTally(m.getTeam2()));
                t1.editions.add(edition);
                t2.editions.add(edition);
                if (m.isDecided()) {
                    t1.add(m.getScore1(), m.getScore2());
                    t2.add(m.getScore2(), m.getScore1());
                }
            }
        }
        return tallies.values().stream()
                .map(RecordTally::toDto)
                .filter(r -> r.played() > 0)
                .sorted(Comparator.comparingInt(TeamRecord::points)
                        .thenComparingInt(r -> r.goalsFor() - r.goalsAgainst())
                        .thenComparingInt(TeamRecord::goalsFor)
                        .reversed()
                        .thenComparing(TeamRecord::teamName))
                .toList();
    }

    /** Stade atteint par chaque selection dans une phase finale (matchs dans l'ordre chronologique). */
    static Map<Team, Integer> editionStages(List<Match> matches, int year) {
        Map<String, LocalDate> phaseStart = new HashMap<>();
        Map<String, Set<Long>> phaseTeams = new HashMap<>();
        for (Match m : matches) {
            if (m.getGroupName() == null) {
                continue;
            }
            String phase = label(m);
            phaseStart.merge(phase, m.getDate() == null ? LocalDate.MAX : m.getDate(), (a, b) -> a.isBefore(b) ? a : b);
            Set<Long> teams = phaseTeams.computeIfAbsent(phase, p -> new HashSet<>());
            teams.add(m.getTeam1().getId());
            teams.add(m.getTeam2().getId());
        }
        List<String> phases = phaseStart.keySet().stream()
                .sorted(Comparator.comparing(phaseStart::get))
                .toList();

        Map<Long, Team> teams = new LinkedHashMap<>();
        Map<Long, Integer> stages = new HashMap<>();
        for (Match m : matches) {
            int stage = stageOf(m, phases, phaseTeams);
            for (Team t : List.of(m.getTeam1(), m.getTeam2())) {
                teams.put(t.getId(), t);
                stages.merge(t.getId(), stage, Math::max);
            }
        }

        List<Match> finals =
                matches.stream().filter(m -> label(m).startsWith("FINALE")).toList();
        if (!finals.isEmpty()) {
            Long winner = finalWinner(finals);
            if (winner != null) {
                stages.put(winner, WINNER);
            }
        } else if (!phases.isEmpty()) {
            roundRobinPodium(matches, phases.get(phases.size() - 1), year, stages);
        }
        Map<Team, Integer> result = new LinkedHashMap<>();
        teams.forEach((id, team) -> result.put(team, stages.get(id)));
        return result;
    }

    private static int stageOf(Match m, List<String> phases, Map<String, Set<Long>> phaseTeams) {
        String label = label(m);
        if (label.startsWith("FINALE")) {
            return RUNNER_UP;
        }
        if (label.contains("3E PLACE") || label.contains("DEMI")) {
            return SEMI;
        }
        if (label.contains("QUART")) {
            return QUARTER;
        }
        if (label.contains("8ES") || label.contains("HUITIEME")) {
            return ROUND_OF_16;
        }
        if (label.contains("16ES") || label.contains("SEIZIEME")) {
            return ROUND_OF_32;
        }
        if (m.getGroupName() != null && phases.indexOf(label) > 0) {
            int size = phaseTeams.get(label).size();
            if (size <= 4) {
                return SEMI;
            }
            if (size <= 12) {
                return QUARTER;
            }
            return size <= 16 ? ROUND_OF_16 : GROUP;
        }
        return GROUP;
    }

    /**
     * Vainqueur de la finale : le match rejoue s'il y en a un, sinon le cumul des manches (finale
     * aller-retour), puis les tirs au but du dernier match. Null si rien ne les departage (ou si une
     * manche n'est pas encore jouee).
     */
    static Long finalWinner(List<Match> finals) {
        boolean replayed = finals.stream().anyMatch(m -> label(m).contains("REJOU"));
        Match last = finals.get(finals.size() - 1);
        Long winner = replayed ? tieWinner(List.of(last)) : null;
        // Match d'appui nul sans tirs au but (Copa America 1979) : difference de buts sur toute la finale.
        return winner != null ? winner : tieWinner(finals);
    }

    private static Long tieWinner(List<Match> counted) {
        Match last = counted.get(counted.size() - 1);
        Long a = last.getTeam1().getId();
        Long b = last.getTeam2().getId();
        int goalsA = 0;
        int goalsB = 0;
        for (Match m : counted) {
            if (!m.isDecided()) {
                return null;
            }
            boolean aHome = m.getTeam1().getId().equals(a);
            goalsA += aHome ? m.getScore1() : m.getScore2();
            goalsB += aHome ? m.getScore2() : m.getScore1();
        }
        if (goalsA != goalsB) {
            return goalsA > goalsB ? a : b;
        }
        if (last.getPenaltyScore1() != null && last.getPenaltyScore2() != null) {
            return last.getPenaltyScore1() > last.getPenaltyScore2() ? a : b;
        }
        return null;
    }

    /** Sans finale : la poule finale, si elle est unique, donne le vainqueur et le finaliste. */
    private static void roundRobinPodium(List<Match> matches, String lastPhase, int year, Map<Long, Integer> stages) {
        List<Match> phase = matches.stream()
                .filter(m -> m.getGroupName() != null && label(m).equals(lastPhase))
                .toList();
        if (phase.stream().map(Match::getGroupName).distinct().count() != 1) {
            return;
        }
        int win = year < 1995 ? 2 : 3;
        Map<Long, int[]> table = new HashMap<>();
        for (Match m : phase) {
            int[] t1 = table.computeIfAbsent(m.getTeam1().getId(), id -> new int[3]);
            int[] t2 = table.computeIfAbsent(m.getTeam2().getId(), id -> new int[3]);
            if (!m.isDecided()) {
                continue;
            }
            int s1 = m.getScore1();
            int s2 = m.getScore2();
            t1[0] += points(s1, s2, win);
            t2[0] += points(s2, s1, win);
            t1[1] += s1 - s2;
            t2[1] += s2 - s1;
            t1[2] += s1;
            t2[2] += s2;
        }
        List<Long> ranking = table.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Long, int[]> e) -> e.getValue()[0])
                        .thenComparing(e -> e.getValue()[1])
                        .thenComparing(e -> e.getValue()[2])
                        .reversed())
                .map(Map.Entry::getKey)
                .toList();
        stages.put(ranking.get(0), WINNER);
        if (ranking.size() > 1) {
            stages.merge(ranking.get(1), RUNNER_UP, Math::max);
        }
    }

    private static int points(int scored, int conceded, int win) {
        if (scored == conceded) {
            return 1;
        }
        return scored > conceded ? win : 0;
    }

    private static String label(Match m) {
        return m.getRoundLabel() == null ? "" : m.getRoundLabel().toUpperCase(Locale.ROOT);
    }

    private static boolean isPlaceholder(Team team) {
        return team.getName() != null && team.getName().toUpperCase(Locale.ROOT).startsWith("A DETERMINER");
    }

    /** "FIFA_WC_1982" -> "1982" ; "CONMEBOL_COPA_1959B" -> "1959B". */
    private static String edition(String code) {
        return code.substring(code.lastIndexOf('_') + 1);
    }

    private static int editionYear(String code) {
        return Integer.parseInt(edition(code).substring(0, 4));
    }

    private static final class RecordTally {
        private final Team team;
        private final Set<String> editions = new HashSet<>();
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;

        private RecordTally(Team team) {
            this.team = team;
        }

        private void add(int scored, int conceded) {
            goalsFor += scored;
            goalsAgainst += conceded;
            if (scored > conceded) {
                won++;
            } else if (scored == conceded) {
                drawn++;
            } else {
                lost++;
            }
        }

        private TeamRecord toDto() {
            return new TeamRecord(
                    team.getId(),
                    team.getName(),
                    editions.size(),
                    won + drawn + lost,
                    won,
                    drawn,
                    lost,
                    goalsFor,
                    goalsAgainst,
                    won * 3 + drawn);
        }
    }

    private static final class JourneyTally {
        private final Team team;
        private final int[] byStage = new int[WINNER + 1];
        private final List<String> titles = new ArrayList<>();

        private JourneyTally(Team team) {
            this.team = team;
        }

        private void add(int stage, String edition) {
            byStage[stage]++;
            if (stage == WINNER) {
                titles.add(edition);
            }
        }

        private Journey toDto() {
            int editions = 0;
            for (int count : byStage) {
                editions += count;
            }
            return new Journey(
                    team.getId(),
                    team.getName(),
                    editions,
                    byStage[WINNER],
                    byStage[RUNNER_UP],
                    byStage[SEMI],
                    byStage[QUARTER],
                    byStage[ROUND_OF_16],
                    byStage[ROUND_OF_32],
                    byStage[GROUP],
                    titles.stream().sorted(Comparator.reverseOrder()).toList());
        }
    }
}
