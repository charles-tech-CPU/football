package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.FinalStanding;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.HeadToHeadCellDto;
import com.charles.footresults.dto.ProjectedStandingRowDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.FinalStandingRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Classement et tete-a-tete ne sont jamais stockes : ils sont recalcules a
 * partir des matchs COMPLETED d'une competition. Points classiques : victoire
 * = 3, nul = 1, defaite = 0. N'a de sens que pour une competition de type
 * LEAGUE (les coupes sont a elimination directe, pas de classement).
 *
 * Certains championnats se scindent en 2e partie de saison en plusieurs
 * mini-groupes (ex: Finlande - top 6 / bottom 6, meme principe en Ecosse,
 * Autriche, Belgique...) : les points de la phase 1 (saison reguliere,
 * TOUS les adversaires) sont conserves tels quels, puis les matchs de la
 * phase 2 (uniquement entre membres du meme groupe final) s'ajoutent par
 * dessus. Comme ce calcul est deja exactement "additionner tous les matchs
 * joues par l'equipe", aucun filtrage particulier n'est necessaire : on
 * garde le meme calcul qu'une competition classique, et on se contente de
 * REPARTIR les lignes obtenues par groupe (TeamCompetitionStatus.groupName)
 * pour l'affichage. Une competition sans aucune equipe groupee garde un
 * classement unique, inchange.
 *
 * Les barrages (promotion/relegation, place europeenne...) sont saisis dans la competition du
 * championnat (round_label contenant "Barrage") mais ne comptent pas au classement : ce sont
 * des confrontations a elimination directe, parfois contre des clubs d'une autre division.
 *
 * Exception : une fois la competition cloturee (cf. CompetitionClosureService), son classement est
 * le classement final fige a la cloture, dans l'ordre valide a la main.
 */
@Service
public class StandingsService {

    private static final int POINTS_WIN = 3;
    private static final int POINTS_DRAW = 1;
    private static final Pattern LEAGUE_ROUND = Pattern.compile("^J(\\d+)$");
    private static final String PLAYOFF_ROUND = "BARRAGE";
    /** Statuts pouvant porter un resultat acquis (cf. Match.isDecided : un forfait doit avoir son score). */
    private static final List<MatchStatus> DECIDED_STATUSES = List.of(MatchStatus.COMPLETED, MatchStatus.FORFEIT);

    private static final Comparator<StandingRowDto> RANKING_ORDER = Comparator.comparingInt(StandingRowDto::points)
            .reversed()
            .thenComparing(
                    Comparator.comparingInt(StandingRowDto::goalDifference).reversed())
            .thenComparing(Comparator.comparingInt(StandingRowDto::goalsFor).reversed());

    private final MatchRepository matchRepository;
    private final TeamCompetitionStatusRepository statusRepository;
    private final CompetitionRepository competitionRepository;
    private final FinalStandingRepository finalStandingRepository;

    public StandingsService(
            MatchRepository matchRepository,
            TeamCompetitionStatusRepository statusRepository,
            CompetitionRepository competitionRepository,
            FinalStandingRepository finalStandingRepository) {
        this.matchRepository = matchRepository;
        this.statusRepository = statusRepository;
        this.competitionRepository = competitionRepository;
        this.finalStandingRepository = finalStandingRepository;
    }

    /**
     * Classement d'une seule phase d'une competition (ex: "Phase de ligue" des
     * coupes d'Europe format 36 clubs), identifiee par un fragment contenu
     * dans le round_label des matchs. Pas de notion de groupe ici : une phase
     * de ligue continentale n'est jamais scindee en mini-groupes comme un
     * championnat national.
     */
    public List<StandingRowDto> computeStandingsForRound(Long competitionId, String roundLabelPart) {
        Map<Long, TeamTally> byTeam = new LinkedHashMap<>();
        for (Match m :
                matchRepository.findByCompetitionIdAndStatusInAndRoundLabelContainingIgnoreCaseOrderByDateAscTimeAsc(
                        competitionId, DECIDED_STATUSES, roundLabelPart)) {
            if (!m.isDecided()) {
                continue;
            }
            tallyFor(byTeam, m.getTeam1()).addResult(m.getScore1(), m.getScore2());
            tallyFor(byTeam, m.getTeam2()).addResult(m.getScore2(), m.getScore1());
        }
        List<StandingRowDto> rows =
                byTeam.values().stream().map(t -> t.toDto(null)).toList();
        return sortRows(rows);
    }

    public List<StandingRowDto> computeStandings(Long competitionId) {
        boolean closed = competitionRepository
                .findById(competitionId)
                .map(Competition::isClosed)
                .orElse(false);
        if (closed) {
            List<FinalStanding> frozen = finalStandingRepository.findByCompetitionIdOrderByFinalRankAsc(competitionId);
            if (!frozen.isEmpty()) {
                return frozen.stream().map(StandingsService::frozenRow).toList();
            }
        }
        return computeLiveStandings(competitionId);
    }

    /** Ligne du classement fige a la cloture, sous la meme forme qu'une ligne calculee. */
    public static StandingRowDto frozenRow(FinalStanding f) {
        Team team = f.getTeam();
        return new StandingRowDto(
                team.getId(),
                team.getName(),
                team.getLogoPath(),
                team.getCountry(),
                f.getPlayed(),
                f.getWon(),
                f.getDrawn(),
                f.getLost(),
                f.getGoalsFor(),
                f.getGoalsAgainst(),
                f.getGoalsFor() - f.getGoalsAgainst(),
                f.getPoints(),
                f.getGroupName());
    }

    /** Classement calcule a partir des matchs, meme pour une competition cloturee. */
    public List<StandingRowDto> computeLiveStandings(Long competitionId) {
        List<Match> played = playedMatches(competitionId);
        if (played.stream().anyMatch(m -> m.getGroupName() != null)) {
            return standingsByMatchGroup(played);
        }

        Map<Long, String> teamGroup = new LinkedHashMap<>();
        // Une equipe inscrite dans un groupe apparait des le depart (0 match, 0 pt),
        // meme avant son premier match : un groupe s'affiche toujours complet. Idem pour
        // toute equipe inscrite a un championnat (saison preparee avant son 1er match).
        Map<Long, TeamTally> byTeam = new LinkedHashMap<>();
        boolean international = false;
        for (TeamCompetitionStatus s : statusRepository.findByCompetitionId(competitionId)) {
            international = s.getCompetition().getType() == CompetitionType.INTERNATIONAL;
            if (s.getGroupName() != null && !s.getGroupName().isBlank()) {
                teamGroup.put(s.getTeam().getId(), s.getGroupName());
                tallyFor(byTeam, s.getTeam());
            } else if (s.getCompetition().getType() == CompetitionType.LEAGUE) {
                tallyFor(byTeam, s.getTeam());
            }
        }

        for (Match m : played) {
            if (international && !isGroupMatch(m, teamGroup)) {
                continue;
            }
            tallyFor(byTeam, m.getTeam1()).addResult(m.getScore1(), m.getScore2());
            tallyFor(byTeam, m.getTeam2()).addResult(m.getScore2(), m.getScore1());
        }

        List<StandingRowDto> rows = byTeam.values().stream()
                .map(t -> t.toDto(teamGroup.get(t.team.getId())))
                .toList();

        if (teamGroup.isEmpty()) {
            return sortRows(rows);
        }

        Map<String, List<StandingRowDto>> byGroup = new LinkedHashMap<>();
        List<StandingRowDto> ungrouped = new ArrayList<>();
        for (StandingRowDto row : rows) {
            if (row.group() == null) {
                ungrouped.add(row);
            } else {
                byGroup.computeIfAbsent(row.group(), g -> new ArrayList<>()).add(row);
            }
        }

        // Championnat scinde : le groupe du haut (plus de points) d'abord. Selections
        // nationales : ordre alphabetique ("Groupe A" avant "Groupe B", "Ligue A - ..."
        // avant "Ligue B - ..."), independant des resultats.
        Comparator<Map.Entry<String, List<StandingRowDto>>> groupOrder = international
                ? Map.Entry.comparingByKey()
                : Comparator.comparingInt((Map.Entry<String, List<StandingRowDto>> e) -> e.getValue().stream()
                                .mapToInt(StandingRowDto::points)
                                .max()
                                .orElse(0))
                        .reversed();
        List<StandingRowDto> result = byGroup.entrySet().stream()
                .sorted(groupOrder)
                .map(e -> sortRows(e.getValue()))
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        result.addAll(sortRows(ungrouped));
        return result;
    }

    /**
     * Groupes portes par les matchs (historique des selections nationales, cf. V115) : un classement
     * par groupe, calcule sur les seuls matchs de ce groupe. Une equipe passee par plusieurs phases
     * de groupes (qualifications en plusieurs tours) figure dans chacun ; les matchs a elimination
     * directe (sans groupe) ne comptent nulle part. Groupes dans l'ordre alphabetique.
     */
    private List<StandingRowDto> standingsByMatchGroup(List<Match> played) {
        Map<String, Map<Long, TeamTally>> byGroup = new TreeMap<>();
        for (Match m : played) {
            if (m.getGroupName() == null) {
                continue;
            }
            Map<Long, TeamTally> byTeam = byGroup.computeIfAbsent(m.getGroupName(), g -> new LinkedHashMap<>());
            tallyFor(byTeam, m.getTeam1()).addResult(m.getScore1(), m.getScore2());
            tallyFor(byTeam, m.getTeam2()).addResult(m.getScore2(), m.getScore1());
        }
        List<StandingRowDto> result = new ArrayList<>();
        byGroup.forEach((group, byTeam) -> result.addAll(
                sortRows(byTeam.values().stream().map(t -> t.toDto(group)).toList())));
        return result;
    }

    private List<StandingRowDto> sortRows(List<StandingRowDto> rows) {
        // Saison pas encore commencee (clubs inscrits, aucun match) : ordre alphabetique.
        if (rows.stream().allMatch(r -> r.played() == 0)) {
            return rows.stream()
                    .sorted(Comparator.comparing(
                            StandingRowDto::teamName, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        }
        return rows.stream().sorted(RANKING_ORDER).toList();
    }

    /**
     * Classement final projete : chaque match pas encore joue (ni COMPLETED, ni forfait avec son
     * score) est simule en attribuant a chaque equipe sa moyenne actuelle de points par match (moyenne
     * du championnat pour une equipe qui n'a encore rien joue). Pas de simulation de buts : a
     * egalite de points projetes, on departage avec le classement actuel.
     *
     * Repart du classement reel (computeStandings, donc meme repartition par groupe) et calcule,
     * groupe par groupe, la fourchette de rangs encore atteignables : pire cas = 0 point sur les
     * matchs restants, meilleur cas = toutes victoires, en traitant chaque equipe
     * independamment (deux equipes qui doivent encore s'affronter ne peuvent pas reellement
     * gagner toutes les deux : fourchette volontairement prudente, jamais trop etroite). Seuls
     * les matchs deja au calendrier sont comptes, plus les journees attendues (totalRounds) pas
     * encore saisies, un match par equipe et par journee : une 2e phase pas encore programmee n'est
     * pas anticipee.
     */
    public List<ProjectedStandingRowDto> computeProjectedStandings(Long competitionId) {
        Map<Long, StandingRowDto> rowsByTeam = new LinkedHashMap<>();
        for (StandingRowDto row : computeStandings(competitionId)) {
            rowsByTeam.put(row.teamId(), row);
        }
        Map<Long, Integer> remaining = new HashMap<>();
        for (Match m : matchRepository.findByCompetitionIdAndStatusNot(competitionId, MatchStatus.COMPLETED)) {
            if (isPlayoff(m) || m.isDecided()) {
                continue;
            }
            for (Team team : List.of(m.getTeam1(), m.getTeam2())) {
                if (isPlaceholder(team)) {
                    continue;
                }
                rowsByTeam.computeIfAbsent(team.getId(), id -> new TeamTally(team).toDto(null));
                remaining.merge(team.getId(), 1, Integer::sum);
            }
        }
        int missingRounds = missingRounds(competitionId);
        if (missingRounds > 0) {
            rowsByTeam.keySet().forEach(id -> remaining.merge(id, missingRounds, Integer::sum));
        }

        int totalPoints =
                rowsByTeam.values().stream().mapToInt(StandingRowDto::points).sum();
        int totalPlayed =
                rowsByTeam.values().stream().mapToInt(StandingRowDto::played).sum();
        double leagueAverage = totalPlayed == 0 ? 0 : (double) totalPoints / totalPlayed;

        // Groupes dans l'ordre du classement actuel (une equipe sans groupe va avec les autres).
        Map<String, List<StandingRowDto>> byGroup = new LinkedHashMap<>();
        for (StandingRowDto row : rowsByTeam.values()) {
            byGroup.computeIfAbsent(row.group(), g -> new ArrayList<>()).add(row);
        }

        List<ProjectedStandingRowDto> result = new ArrayList<>();
        for (List<StandingRowDto> groupRows : byGroup.values()) {
            List<StandingRowDto> current = sortRows(groupRows);
            List<Outlook> outlooks = new ArrayList<>();
            for (int i = 0; i < current.size(); i++) {
                StandingRowDto row = current.get(i);
                int left = remaining.getOrDefault(row.teamId(), 0);
                double average = row.played() == 0 ? leagueAverage : (double) row.points() / row.played();
                outlooks.add(new Outlook(row, i + 1, left, row.points() + average * left));
            }
            for (Outlook o : outlooks) {
                o.bestRank = 1
                        + (int) outlooks.stream()
                                .filter(other -> other.alwaysAbove(o))
                                .count();
                o.worstRank = outlooks.size()
                        - (int) outlooks.stream().filter(o::alwaysAbove).count();
            }
            outlooks.stream()
                    .sorted(Comparator.comparingDouble((Outlook o) -> o.projectedPoints)
                            .reversed()
                            .thenComparingInt(o -> o.currentRank))
                    .map(o -> ProjectedStandingRowDto.of(
                            o.row, o.remaining, o.projectedPoints, o.currentRank, o.bestRank, o.worstRank))
                    .forEach(result::add);
        }
        return result;
    }

    /** Journees attendues (totalRounds) au-dela de la derniere journee "J12" deja saisie. */
    private int missingRounds(Long competitionId) {
        Integer totalRounds = competitionRepository
                .findById(competitionId)
                .map(Competition::getTotalRounds)
                .orElse(null);
        if (totalRounds == null) {
            return 0;
        }
        int lastCreated = 0;
        for (Match m : matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(competitionId)) {
            Matcher round = LEAGUE_ROUND.matcher(m.getRoundLabel() == null ? "" : m.getRoundLabel());
            if (round.matches()) {
                lastCreated = Math.max(lastCreated, Integer.parseInt(round.group(1)));
            }
        }
        return Math.max(0, totalRounds - lastCreated);
    }

    /** Places generiques "A DETERMINER ..." (tirage a venir) : pas de vraies equipes a classer. */
    private boolean isPlaceholder(Team team) {
        return team.getName() != null && team.getName().toUpperCase().startsWith("A DETERMINER");
    }

    public List<HeadToHeadCellDto> computeHeadToHead(Long competitionId) {
        Map<Long, Map<Long, int[]>> tally = new LinkedHashMap<>();

        for (Match m : playedMatches(competitionId)) {
            addH2H(tally, m.getTeam1().getId(), m.getTeam2().getId(), m.getScore1(), m.getScore2());
            addH2H(tally, m.getTeam2().getId(), m.getTeam1().getId(), m.getScore2(), m.getScore1());
        }

        List<HeadToHeadCellDto> result = new ArrayList<>();
        tally.forEach((teamAId, opponents) -> opponents.forEach(
                (teamBId, wdl) -> result.add(new HeadToHeadCellDto(teamAId, teamBId, wdl[0], wdl[1], wdl[2]))));
        return result;
    }

    /** Matchs joues (forfaits sur tapis vert compris) comptant au classement : tous sauf les barrages. */
    private List<Match> playedMatches(Long competitionId) {
        return matchRepository
                .findByCompetitionIdAndStatusInOrderByDateAscTimeAsc(competitionId, DECIDED_STATUSES)
                .stream()
                .filter(m -> m.isDecided() && !isPlayoff(m))
                .toList();
    }

    /** Match de barrage ("Barrage (2E-5E)", "Barrages - Aller"...), hors classement. */
    private static boolean isPlayoff(Match m) {
        return m.getRoundLabel() != null && m.getRoundLabel().toUpperCase().contains(PLAYOFF_ROUND);
    }

    /**
     * Selections nationales : seuls les matchs de groupe comptent au classement (deux equipes du meme
     * groupe, journee "J<n>" ou tour non renseigne). Play-in, quarts de finale, finales... opposent des
     * equipes de groupes ou de ligues differents et restent hors classement.
     */
    private static boolean isGroupMatch(Match m, Map<Long, String> teamGroup) {
        String group = teamGroup.get(m.getTeam1().getId());
        return group != null
                && group.equals(teamGroup.get(m.getTeam2().getId()))
                && (m.getRoundLabel() == null
                        || LEAGUE_ROUND.matcher(m.getRoundLabel()).matches());
    }

    private TeamTally tallyFor(Map<Long, TeamTally> byTeam, Team team) {
        return byTeam.computeIfAbsent(team.getId(), id -> new TeamTally(team));
    }

    private void addH2H(Map<Long, Map<Long, int[]>> tally, Long teamAId, Long teamBId, Integer goalsA, Integer goalsB) {
        if (goalsA == null || goalsB == null) {
            return;
        }
        int[] wdl =
                tally.computeIfAbsent(teamAId, id -> new LinkedHashMap<>()).computeIfAbsent(teamBId, id -> new int[3]);
        if (goalsA > goalsB) {
            wdl[0]++;
        } else if (goalsA.equals(goalsB)) {
            wdl[1]++;
        } else {
            wdl[2]++;
        }
    }

    /** Situation d'une equipe dans la projection : points acquis, matchs restants, rangs possibles. */
    private static final class Outlook {
        private final StandingRowDto row;
        private final int currentRank;
        private final int remaining;
        private final double projectedPoints;
        private int bestRank;
        private int worstRank;

        private Outlook(StandingRowDto row, int currentRank, int remaining, double projectedPoints) {
            this.row = row;
            this.currentRank = currentRank;
            this.remaining = remaining;
            this.projectedPoints = projectedPoints;
        }

        private int maxPoints() {
            return row.points() + POINTS_WIN * remaining;
        }

        /**
         * Vrai si cette equipe finit devant "other" quels que soient les matchs restants : meme
         * en perdant tout, elle depasse le total maximal de l'autre. A egalite parfaite, seul un
         * classement definitif (plus aucun match pour l'une ni l'autre) tranche via les criteres
         * actuels (difference de buts...), sinon les buts restent a jouer.
         */
        private boolean alwaysAbove(Outlook other) {
            if (other == this) {
                return false;
            }
            int min = row.points();
            if (min != other.maxPoints()) {
                return min > other.maxPoints();
            }
            return remaining == 0 && other.remaining == 0 && currentRank < other.currentRank;
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

        private StandingRowDto toDto(String group) {
            int points = won * POINTS_WIN + drawn * POINTS_DRAW;
            return new StandingRowDto(
                    team.getId(),
                    team.getName(),
                    team.getLogoPath(),
                    team.getCountry(),
                    played,
                    won,
                    drawn,
                    lost,
                    goalsFor,
                    goalsAgainst,
                    goalsFor - goalsAgainst,
                    points,
                    group);
        }
    }
}
