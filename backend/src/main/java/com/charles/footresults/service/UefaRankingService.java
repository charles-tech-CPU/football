package com.charles.footresults.service;

import com.charles.footresults.domain.ClubUefaRanking;
import com.charles.footresults.domain.CountryUefaRanking;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.dto.ClubUefaRankingDto;
import com.charles.footresults.dto.CountryUefaRankingDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.dto.UefaHistoryPointDto;
import com.charles.footresults.repository.ClubUefaRankingRepository;
import com.charles.footresults.repository.CountryUefaRankingRepository;
import com.charles.footresults.repository.MatchRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Classements UEFA (clubs et pays), importes des onglets UEFA/PAYS du fichier Excel source
 * (voir import/extract_uefa_rankings.py) a l'exception de tout ce qui concerne la saison en
 * cours : contrairement aux autres colonnes (figees a l'import), les points "2027", le badge
 * de coupe en cours d'un club et le nombre de clubs "encore en course" par pays sont toujours
 * recalcules ici a partir des matchs LDC/EL/EC deja saisis dans l'appli, pour evoluer au fil
 * de la saison (cf. README section 8 + discussion produit : un club elimine perd son badge et
 * n'est plus compte dans son pays, et CHAQUE match joue rapporte des points, pas seulement la
 * phase de ligue).
 *
 * Bareme des points (par match individuel, aller et retour comptent chacun separement) :
 * - tours de qualification (1er/2e/3e tour, "voie principale" CE) et barrage AVANT la phase de
 *   ligue : victoire = 1 pt, nul = 0.5 pt.
 * - phase de ligue, barrage APRES la phase de ligue (9e-24e) et phases finales (seizieme a
 *   finale) : victoire = 2 pts, nul = 1 pt.
 * Un "barrage" existe aux 2 moments de la saison sous le meme libelle ; on les distingue par
 * date (avant/apres le debut de la phase de ligue de cette competition), cf. categorize().
 *
 * Statut "encore en course" : un club est elimine des qu'il perd une confrontation a
 * elimination directe (aggregat aller-retour, ou tirs au but si egalite) deja entierement
 * jouee, ou, pour la phase de ligue (pas une confrontation mais une mini-ligue), une fois que
 * TOUTE la phase de ligue de sa competition est terminee et que son classement le place parmi
 * les 12 derniers (rang > 24 sur 36). Volontairement pas d'elimination mathematique anticipee
 * (avant la fin reelle de la phase) ni de suivi du "repechage" vers une coupe inferieure en cas
 * d'echec en qualifs : hors scope, cf. simplifications deja actees dans le README.
 */
@Service
public class UefaRankingService {

    private static final List<String> CONTINENTAL_CODES = List.of("LDC", "EL", "EC");
    private static final String LEAGUE_PHASE_ROUND = "PHASE DE LIGUE";
    // Rang a partir duquel un club est elimine de la phase de ligue (format 36 clubs : 8
    // qualifies directs + 16 en barrage = 24 encore en vie, 12 elimines) : meme seuil que les
    // "rank-bands" du classement de phase de ligue cote frontend (CompetitionDetailView).
    private static final int LEAGUE_PHASE_ELIMINATION_RANK = 24;

    private static final BigDecimal WIN_QUALIFYING = BigDecimal.ONE;
    private static final BigDecimal DRAW_QUALIFYING = new BigDecimal("0.5");
    private static final BigDecimal WIN_MAIN = BigDecimal.valueOf(2);
    private static final BigDecimal DRAW_MAIN = BigDecimal.ONE;
    private static final BigDecimal SEASONS_IN_COEFFICIENT = BigDecimal.valueOf(5);

    private static final Pattern LEG_SUFFIX = Pattern.compile("-\\s*(ALLER|RETOUR)\\s*$", Pattern.CASE_INSENSITIVE);

    private enum RoundCategory {
        QUALIFYING_TIE,
        PRE_LEAGUE_BARRAGE,
        LEAGUE_PHASE,
        POST_LEAGUE_BARRAGE,
        KNOCKOUT_TIE,
        UNKNOWN
    }

    private final ClubUefaRankingRepository clubRepository;
    private final CountryUefaRankingRepository countryRepository;
    private final MatchRepository matchRepository;
    private final StandingsService standingsService;

    public UefaRankingService(
            ClubUefaRankingRepository clubRepository,
            CountryUefaRankingRepository countryRepository,
            MatchRepository matchRepository,
            StandingsService standingsService) {
        this.clubRepository = clubRepository;
        this.countryRepository = countryRepository;
        this.matchRepository = matchRepository;
        this.standingsService = standingsService;
    }

    public List<ClubUefaRankingDto> findClubRankings() {
        SeasonState state = computeSeasonState();
        return clubRepository.findAllByOrderByUefaRankAsc().stream()
                .map(r -> toDto(r, state))
                .toList();
    }

    /** Points 2027 d'un pays = somme des points 2027 de ses clubs ; "encore en course" = somme des clubs vivants. */
    public List<CountryUefaRankingDto> findCountryRankings() {
        SeasonState state = computeSeasonState();
        Map<String, BigDecimal> pointsByCountry = new HashMap<>();
        Map<String, int[]> aliveCountByCountry = new HashMap<>(); // [ldc, el, ec]
        for (ClubUefaRanking r : clubRepository.findAll()) {
            if (r.getCountry() == null) {
                continue;
            }
            Long teamId = r.getTeam() != null ? r.getTeam().getId() : null;
            BigDecimal earned =
                    teamId != null ? state.pointsByTeam.getOrDefault(teamId, BigDecimal.ZERO) : BigDecimal.ZERO;
            pointsByCountry.merge(r.getCountry(), earned, BigDecimal::add);

            String liveCup = liveCurrentCup(r, state);
            if (liveCup != null) {
                int[] counts = aliveCountByCountry.computeIfAbsent(r.getCountry(), k -> new int[3]);
                switch (liveCup) {
                    case "LDC" -> counts[0]++;
                    case "EL" -> counts[1]++;
                    case "EC" -> counts[2]++;
                    default -> {
                        // Autre code de competition : rien a decompter
                    }
                }
            }
        }
        return countryRepository.findAllByOrderByUefaRankAsc().stream()
                .map(r -> toDto(
                        r,
                        pointsByCountry.getOrDefault(r.getCountry(), BigDecimal.ZERO),
                        aliveCountByCountry.getOrDefault(r.getCountry(), new int[3])))
                .toList();
    }

    /**
     * Evolution du coefficient d'un club (id de sa ligne de classement) au fil de la saison.
     * Rien n'est stocke : les points de la saison en cours etant deja recalcules a partir des
     * matchs dates, on rejoue simplement ces matchs dans l'ordre chronologique (meme bareme que
     * findClubRankings), ce qui donne tout l'historique, y compris avant la mise en place de
     * cette courbe. Coefficient club = somme des 5 saisons (meme formule que la colonne "Total"
     * du fichier Excel source).
     */
    public List<UefaHistoryPointDto> findClubHistory(Long clubRankingId) {
        ClubUefaRanking club = clubRepository
                .findById(clubRankingId)
                .orElseThrow(() -> new EntityNotFoundException("Club introuvable : " + clubRankingId));
        BigDecimal pastSeasons = nonNull(club.getPoints2026())
                .add(nonNull(club.getPoints2025()))
                .add(nonNull(club.getPoints2024()))
                .add(nonNull(club.getPoints2023()));
        Set<Long> teamIds = club.getTeam() != null ? Set.of(club.getTeam().getId()) : Set.of();
        return history(teamIds, pastSeasons::add);
    }

    /**
     * Evolution du coefficient d'un pays (id de sa ligne de classement). Points de saison = somme
     * des points de ses clubs (comme findCountryRankings) ; coefficient = moyenne sur 5 saisons
     * des points de saison divises par le nombre de clubs engages cette saison-la (meme formule
     * que la colonne "Total" de l'onglet PAYS du fichier Excel source).
     */
    public List<UefaHistoryPointDto> findCountryHistory(Long countryRankingId) {
        CountryUefaRanking country = countryRepository
                .findById(countryRankingId)
                .orElseThrow(() -> new EntityNotFoundException("Pays introuvable : " + countryRankingId));
        Set<Long> teamIds = new HashSet<>();
        for (ClubUefaRanking club : clubRepository.findAll()) {
            if (club.getTeam() != null && country.getCountry().equals(club.getCountry())) {
                teamIds.add(club.getTeam().getId());
            }
        }
        BigDecimal pastSeasons = perClub(country.getPoints2026(), country.getNb2026())
                .add(perClub(country.getPoints2025(), country.getNb2025()))
                .add(perClub(country.getPoints2024(), country.getNb2024()))
                .add(perClub(country.getPoints2023(), country.getNb2023()));
        return history(
                teamIds,
                season -> pastSeasons
                        .add(perClub(season, country.getNb2027()))
                        .divide(SEASONS_IN_COEFFICIENT, MathContext.DECIMAL64));
    }

    /**
     * Points de saison cumules des equipes donnees, un point par date ou l'une d'elles a joue
     * (meme une defaite : la courbe montre chaque journee europeenne). Le premier point (date
     * null) regroupe les matchs sans date (tours de qualif. sans date exploitable), 0 sinon.
     */
    private List<UefaHistoryPointDto> history(Set<Long> teamIds, UnaryOperator<BigDecimal> coefficientOf) {
        List<Match> matches = matchRepository.findByCompetition_CodeIn(CONTINENTAL_CODES);
        Map<Long, LocalDate> leaguePhaseStart = leaguePhaseStartByCompetition(matches);
        BigDecimal undated = BigDecimal.ZERO;
        TreeMap<LocalDate, BigDecimal> byDate = new TreeMap<>();
        for (Match m : matches) {
            if (!hasFinalScore(m)) {
                continue;
            }
            RoundCategory category = categorize(m, leaguePhaseStart);
            BigDecimal earned = BigDecimal.ZERO;
            boolean involved = false;
            if (teamIds.contains(m.getTeam1().getId())) {
                involved = true;
                earned = earned.add(nonNull(pointsEarned(category, m.getScore1(), m.getScore2())));
            }
            if (teamIds.contains(m.getTeam2().getId())) {
                involved = true;
                earned = earned.add(nonNull(pointsEarned(category, m.getScore2(), m.getScore1())));
            }
            if (!involved) {
                continue;
            }
            if (m.getDate() == null) {
                undated = undated.add(earned);
            } else {
                byDate.merge(m.getDate(), earned, BigDecimal::add);
            }
        }

        List<UefaHistoryPointDto> points = new ArrayList<>();
        BigDecimal cumulative = undated;
        points.add(historyPoint(null, cumulative, coefficientOf));
        for (Map.Entry<LocalDate, BigDecimal> entry : byDate.entrySet()) {
            cumulative = cumulative.add(entry.getValue());
            points.add(historyPoint(entry.getKey(), cumulative, coefficientOf));
        }
        return points;
    }

    private UefaHistoryPointDto historyPoint(
            LocalDate date, BigDecimal seasonPoints, UnaryOperator<BigDecimal> coefficientOf) {
        return new UefaHistoryPointDto(
                date, seasonPoints, coefficientOf.apply(seasonPoints).setScale(3, RoundingMode.HALF_UP));
    }

    private static BigDecimal nonNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /** Points d'une saison ramenes au nombre de clubs engages ; 0 si inconnu. */
    private static BigDecimal perClub(BigDecimal points, Integer clubs) {
        if (points == null || clubs == null || clubs == 0) {
            return BigDecimal.ZERO;
        }
        return points.divide(BigDecimal.valueOf(clubs), MathContext.DECIMAL64);
    }

    /** Etat de la saison en cours pour les 3 coupes d'Europe, calcule une seule fois par requete. */
    private SeasonState computeSeasonState() {
        List<Match> matches = matchRepository.findByCompetition_CodeIn(CONTINENTAL_CODES);
        Map<Long, LocalDate> leaguePhaseStart = leaguePhaseStartByCompetition(matches);

        Map<Long, BigDecimal> pointsByTeam = new HashMap<>();
        Map<TieKey, List<Match>> ties = new HashMap<>();
        Map<Long, List<Match>> leaguePhaseByCompetition = new HashMap<>();

        for (Match m : matches) {
            RoundCategory category = categorize(m, leaguePhaseStart);
            if (category == RoundCategory.LEAGUE_PHASE) {
                leaguePhaseByCompetition
                        .computeIfAbsent(m.getCompetition().getId(), k -> new ArrayList<>())
                        .add(m);
            }
            if (isTieCategory(category)) {
                TieKey key = tieKeyFor(m, category);
                ties.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
            }
            if (hasFinalScore(m)) {
                addResult(pointsByTeam, m.getTeam1().getId(), category, m.getScore1(), m.getScore2());
                addResult(pointsByTeam, m.getTeam2().getId(), category, m.getScore2(), m.getScore1());
            }
        }

        Set<Long> eliminated = new HashSet<>();
        eliminated.addAll(tieLosers(ties.values()));
        eliminated.addAll(leaguePhaseEliminated(leaguePhaseByCompetition));
        return new SeasonState(pointsByTeam, eliminated);
    }

    /** Date du premier match de phase de ligue de chaque competition (sert a situer les barrages). */
    private Map<Long, LocalDate> leaguePhaseStartByCompetition(List<Match> matches) {
        Map<Long, LocalDate> leaguePhaseStart = new HashMap<>();
        for (Match m : matches) {
            if (m.getDate() != null && LEAGUE_PHASE_ROUND.equalsIgnoreCase(trim(m.getRoundLabel()))) {
                leaguePhaseStart.merge(m.getCompetition().getId(), m.getDate(), (a, b) -> a.isBefore(b) ? a : b);
            }
        }
        return leaguePhaseStart;
    }

    /** Perdants des confrontations a elimination directe deja entierement jouees. */
    private Set<Long> tieLosers(Collection<List<Match>> ties) {
        Set<Long> losers = new HashSet<>();
        for (List<Match> legs : ties) {
            legs.sort(Comparator.comparing(Match::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
            Long loser = tieLoser(legs);
            if (loser != null) {
                losers.add(loser);
            }
        }
        return losers;
    }

    /** Clubs classes au-dela du rang d'elimination, pour chaque phase de ligue entierement terminee. */
    private Set<Long> leaguePhaseEliminated(Map<Long, List<Match>> leaguePhaseByCompetition) {
        Set<Long> eliminated = new HashSet<>();
        for (Map.Entry<Long, List<Match>> entry : leaguePhaseByCompetition.entrySet()) {
            boolean complete = entry.getValue().stream().allMatch(m -> m.getStatus() == MatchStatus.COMPLETED);
            if (complete) {
                List<StandingRowDto> standings =
                        standingsService.computeStandingsForRound(entry.getKey(), LEAGUE_PHASE_ROUND);
                for (int i = LEAGUE_PHASE_ELIMINATION_RANK; i < standings.size(); i++) {
                    eliminated.add(standings.get(i).teamId());
                }
            }
        }
        return eliminated;
    }

    private boolean hasFinalScore(Match m) {
        return m.getStatus() == MatchStatus.COMPLETED && m.getScore1() != null && m.getScore2() != null;
    }

    private boolean isTieCategory(RoundCategory category) {
        return category == RoundCategory.QUALIFYING_TIE
                || category == RoundCategory.PRE_LEAGUE_BARRAGE
                || category == RoundCategory.POST_LEAGUE_BARRAGE
                || category == RoundCategory.KNOCKOUT_TIE;
    }

    private RoundCategory categorize(Match m, Map<Long, LocalDate> leaguePhaseStart) {
        String label = trim(m.getRoundLabel());
        if (label.contains(LEAGUE_PHASE_ROUND)) {
            return RoundCategory.LEAGUE_PHASE;
        }
        if (label.contains("SEIZIEME")
                || label.contains("HUITIEME")
                || label.contains("HUITEME")
                || label.contains("QUART")
                || label.contains("DEMI")
                || label.contains("FINALE")) {
            return RoundCategory.KNOCKOUT_TIE;
        }
        if (label.contains("BARRAGE")) {
            LocalDate start = leaguePhaseStart.get(m.getCompetition().getId());
            boolean postLeague =
                    start != null && m.getDate() != null && !m.getDate().isBefore(start);
            return postLeague ? RoundCategory.POST_LEAGUE_BARRAGE : RoundCategory.PRE_LEAGUE_BARRAGE;
        }
        if (label.contains("QUALIF") || label.contains("VOIE PRINCIPALE")) {
            return RoundCategory.QUALIFYING_TIE;
        }
        return RoundCategory.UNKNOWN;
    }

    private String trim(String label) {
        return label == null ? "" : label.toUpperCase();
    }

    private void addResult(
            Map<Long, BigDecimal> points, Long teamId, RoundCategory category, int goalsFor, int goalsAgainst) {
        BigDecimal earned = pointsEarned(category, goalsFor, goalsAgainst);
        if (earned != null) {
            points.merge(teamId, earned, BigDecimal::add);
        }
    }

    /** Points rapportes par un match selon le bareme de son tour ; null si le tour n'en rapporte pas. */
    private BigDecimal pointsEarned(RoundCategory category, int goalsFor, int goalsAgainst) {
        BigDecimal win =
                switch (category) {
                    case QUALIFYING_TIE, PRE_LEAGUE_BARRAGE -> WIN_QUALIFYING;
                    case LEAGUE_PHASE, POST_LEAGUE_BARRAGE, KNOCKOUT_TIE -> WIN_MAIN;
                    default -> null;
                };
        if (win == null) {
            return null;
        }
        BigDecimal draw = (category == RoundCategory.QUALIFYING_TIE || category == RoundCategory.PRE_LEAGUE_BARRAGE)
                ? DRAW_QUALIFYING
                : DRAW_MAIN;
        if (goalsFor > goalsAgainst) {
            return win;
        }
        return goalsFor == goalsAgainst ? draw : BigDecimal.ZERO;
    }

    private TieKey tieKeyFor(Match m, RoundCategory category) {
        String stripped =
                LEG_SUFFIX.matcher(trim(m.getRoundLabel())).replaceAll("").trim();
        long lo = Math.min(m.getTeam1().getId(), m.getTeam2().getId());
        long hi = Math.max(m.getTeam1().getId(), m.getTeam2().getId());
        return new TieKey(m.getCompetition().getId(), category, stripped, lo, hi);
    }

    /** Vainqueur d'une confrontation (1 ou plusieurs manches) une fois TOUTES les manches jouees ; null si pas encore decidee. */
    private Long tieLoser(List<Match> legs) {
        if (!legs.stream().allMatch(this::hasFinalScore)) {
            return null;
        }
        Match first = legs.get(0);
        long teamA = first.getTeam1().getId();
        long teamB = first.getTeam2().getId();
        int aggA = legs.stream().mapToInt(m -> goalsOf(m, teamA)).sum();
        int aggB = legs.stream().mapToInt(m -> goalsOf(m, teamB)).sum();
        if (aggA != aggB) {
            return loserOf(teamA, aggA, teamB, aggB);
        }
        Match decider = legs.get(legs.size() - 1);
        if (decider.getPenaltyScore1() == null || decider.getPenaltyScore2() == null) {
            return null;
        }
        return loserOf(teamA, penaltiesOf(decider, teamA), teamB, penaltiesOf(decider, teamB));
    }

    private int goalsOf(Match m, long teamId) {
        return m.getTeam1().getId() == teamId ? m.getScore1() : m.getScore2();
    }

    private int penaltiesOf(Match m, long teamId) {
        return m.getTeam1().getId() == teamId ? m.getPenaltyScore1() : m.getPenaltyScore2();
    }

    /** Equipe au plus petit score, ou null en cas d'egalite. */
    private Long loserOf(long teamA, int scoreA, long teamB, int scoreB) {
        if (scoreA == scoreB) {
            return null;
        }
        return scoreA > scoreB ? teamB : teamA;
    }

    /** Coupe actuellement jouee par ce club, ou null s'il en a ete elimine (ou n'en joue aucune). */
    private String liveCurrentCup(ClubUefaRanking r, SeasonState state) {
        if (r.getCurrentCup() == null) {
            return null;
        }
        Long teamId = r.getTeam() != null ? r.getTeam().getId() : null;
        if (teamId != null && state.eliminatedTeamIds.contains(teamId)) {
            return null;
        }
        return r.getCurrentCup();
    }

    private ClubUefaRankingDto toDto(ClubUefaRanking r, SeasonState state) {
        Long teamId = r.getTeam() != null ? r.getTeam().getId() : null;
        BigDecimal points2027 =
                teamId != null ? state.pointsByTeam.getOrDefault(teamId, BigDecimal.ZERO) : BigDecimal.ZERO;
        return new ClubUefaRankingDto(
                r.getId(),
                r.getUefaRank(),
                teamId,
                r.getClubName(),
                r.getTeam() != null ? r.getTeam().getLogoPath() : null,
                r.getCountry(),
                liveCurrentCup(r, state),
                r.getTotal(),
                points2027,
                r.getPoints2026(),
                r.getPoints2025(),
                r.getPoints2024(),
                r.getPoints2023());
    }

    private CountryUefaRankingDto toDto(CountryUefaRanking r, BigDecimal points2027, int[] aliveCounts) {
        return new CountryUefaRankingDto(
                r.getId(),
                r.getUefaRank(),
                r.getCountry(),
                r.getTotal(),
                points2027,
                r.getPoints2026(),
                r.getPoints2025(),
                r.getPoints2024(),
                r.getPoints2023(),
                aliveCounts[0],
                aliveCounts[1],
                aliveCounts[2],
                r.getLdcDebut(),
                r.getElDebut(),
                r.getEcDebut(),
                r.getNb2027(),
                r.getNb2026(),
                r.getNb2025(),
                r.getNb2024(),
                r.getNb2023(),
                colorCode(aliveCounts));
    }

    private String colorCode(int[] aliveCounts) {
        if (aliveCounts[0] > 0) return "BLUE";
        if (aliveCounts[1] > 0) return "ORANGE";
        if (aliveCounts[2] > 0) return "YELLOW";
        return "RED";
    }

    private record TieKey(
            Long competitionId, RoundCategory category, String strippedLabel, long teamLow, long teamHigh) {}

    private record SeasonState(Map<Long, BigDecimal> pointsByTeam, Set<Long> eliminatedTeamIds) {}
}
