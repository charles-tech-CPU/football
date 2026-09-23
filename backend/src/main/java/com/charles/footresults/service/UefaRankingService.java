package com.charles.footresults.service;

import com.charles.footresults.domain.ClubUefaRanking;
import com.charles.footresults.domain.CountryUefaRanking;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.dto.ClubUefaRankingDto;
import com.charles.footresults.dto.CountryUefaRankingDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.ClubUefaRankingRepository;
import com.charles.footresults.repository.CountryUefaRankingRepository;
import com.charles.footresults.repository.MatchRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                    default -> {}
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

    /** Etat de la saison en cours pour les 3 coupes d'Europe, calcule une seule fois par requete. */
    private SeasonState computeSeasonState() {
        List<Match> matches = matchRepository.findByCompetition_CodeIn(CONTINENTAL_CODES);

        Map<Long, LocalDate> leaguePhaseStart = new HashMap<>();
        for (Match m : matches) {
            if (m.getDate() != null && LEAGUE_PHASE_ROUND.equalsIgnoreCase(trim(m.getRoundLabel()))) {
                leaguePhaseStart.merge(m.getCompetition().getId(), m.getDate(), (a, b) -> a.isBefore(b) ? a : b);
            }
        }

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
            if (m.getStatus() == MatchStatus.COMPLETED && m.getScore1() != null && m.getScore2() != null) {
                addResult(pointsByTeam, m.getTeam1().getId(), category, m.getScore1(), m.getScore2());
                addResult(pointsByTeam, m.getTeam2().getId(), category, m.getScore2(), m.getScore1());
            }
        }

        Set<Long> eliminated = new HashSet<>();
        for (List<Match> legs : ties.values()) {
            legs.sort(Comparator.comparing(Match::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
            Long loser = tieLoser(legs);
            if (loser != null) {
                eliminated.add(loser);
            }
        }

        for (Map.Entry<Long, List<Match>> entry : leaguePhaseByCompetition.entrySet()) {
            boolean complete = entry.getValue().stream().allMatch(m -> m.getStatus() == MatchStatus.COMPLETED);
            if (!complete) {
                continue;
            }
            List<StandingRowDto> standings =
                    standingsService.computeStandingsForRound(entry.getKey(), LEAGUE_PHASE_ROUND);
            for (int i = LEAGUE_PHASE_ELIMINATION_RANK; i < standings.size(); i++) {
                eliminated.add(standings.get(i).teamId());
            }
        }

        return new SeasonState(pointsByTeam, eliminated);
    }

    private boolean isTieCategory(RoundCategory category) {
        return category == RoundCategory.QUALIFYING_TIE
                || category == RoundCategory.PRE_LEAGUE_BARRAGE
                || category == RoundCategory.POST_LEAGUE_BARRAGE
                || category == RoundCategory.KNOCKOUT_TIE;
    }

    private RoundCategory categorize(Match m, Map<Long, LocalDate> leaguePhaseStart) {
        String label = trim(m.getRoundLabel());
        if (label.contains("PHASE DE LIGUE")) {
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
        BigDecimal win =
                switch (category) {
                    case QUALIFYING_TIE, PRE_LEAGUE_BARRAGE -> WIN_QUALIFYING;
                    case LEAGUE_PHASE, POST_LEAGUE_BARRAGE, KNOCKOUT_TIE -> WIN_MAIN;
                    default -> null;
                };
        if (win == null) {
            return;
        }
        BigDecimal draw = (category == RoundCategory.QUALIFYING_TIE || category == RoundCategory.PRE_LEAGUE_BARRAGE)
                ? DRAW_QUALIFYING
                : DRAW_MAIN;
        BigDecimal earned = goalsFor > goalsAgainst ? win : goalsFor == goalsAgainst ? draw : BigDecimal.ZERO;
        points.merge(teamId, earned, BigDecimal::add);
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
        for (Match m : legs) {
            if (m.getStatus() != MatchStatus.COMPLETED || m.getScore1() == null || m.getScore2() == null) {
                return null;
            }
        }
        Match first = legs.get(0);
        long teamA = first.getTeam1().getId();
        long teamB = first.getTeam2().getId();
        int aggA = 0, aggB = 0;
        for (Match m : legs) {
            if (m.getTeam1().getId() == teamA) {
                aggA += m.getScore1();
                aggB += m.getScore2();
            } else {
                aggA += m.getScore2();
                aggB += m.getScore1();
            }
        }
        if (aggA != aggB) {
            return aggA > aggB ? teamB : teamA;
        }
        Match decider = legs.get(legs.size() - 1);
        if (decider.getPenaltyScore1() != null && decider.getPenaltyScore2() != null) {
            int penA, penB;
            if (decider.getTeam1().getId() == teamA) {
                penA = decider.getPenaltyScore1();
                penB = decider.getPenaltyScore2();
            } else {
                penA = decider.getPenaltyScore2();
                penB = decider.getPenaltyScore1();
            }
            if (penA != penB) {
                return penA > penB ? teamB : teamA;
            }
        }
        return null;
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
