package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchCreateDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.MatchPageDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchService {

    /** Fragment du round_label des matchs de phase de ligue des coupes d'Europe (cf. UefaRankingService). */
    private static final String LEAGUE_PHASE_ROUND = "PHASE DE LIGUE";

    /** Suffixe de manche d'une confrontation aller/retour (cf. UefaRankingService). */
    private static final Pattern LEG_SUFFIX = Pattern.compile("-\s*(ALLER|RETOUR)\s*$", Pattern.CASE_INSENSITIVE);

    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final StandingsService standingsService;
    private final FeaturedMatchService featuredMatchService;

    public MatchService(
            MatchRepository matchRepository,
            CompetitionRepository competitionRepository,
            TeamRepository teamRepository,
            StandingsService standingsService,
            FeaturedMatchService featuredMatchService) {
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.standingsService = standingsService;
        this.featuredMatchService = featuredMatchService;
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
        Map<Long, Integer> ranks = leagueRanks(result.getContent());
        Map<Long, Map<Long, Integer>> cupRanks = continentalRanks(result.getContent());
        Set<Long> featured = featuredMatchService.featuredMatchIds(result.getContent(), ranks);
        List<MatchDto> items = result.getContent().stream()
                .map(m -> {
                    Map<Long, Integer> r =
                            cupRanks.getOrDefault(m.getCompetition().getId(), ranks);
                    return MatchDto.from(m)
                            .withCalendarInfo(
                                    r.get(m.getTeam1().getId()),
                                    r.get(m.getTeam2().getId()),
                                    featured.contains(m.getId()),
                                    knockoutInfo(m));
                })
                .toList();
        return new MatchPageDto(items, result.getTotalElements());
    }

    /**
     * Confrontation a elimination directe d'un match de coupe (nationale ou d'Europe, hors phase
     * de ligue) : manches = matchs de la meme competition entre les 2 memes equipes, dans le meme
     * tour (round_label sans son suffixe "- ALLER"/"- RETOUR"). Le match decisif est la derniere
     * manche (un match marque "ALLER" ne l'est jamais) ; null pour un match de championnat.
     */
    private MatchDto.Knockout knockoutInfo(Match m) {
        Competition c = m.getCompetition();
        boolean cup = c.getType() == CompetitionType.DOMESTIC_CUP || c.getType() == CompetitionType.CONTINENTAL_CUP;
        String label = m.getRoundLabel() == null ? "" : m.getRoundLabel().trim();
        if (!cup || label.toUpperCase().contains(LEAGUE_PHASE_ROUND)) {
            return null;
        }
        String tour = LEG_SUFFIX.matcher(label).replaceAll("").trim();
        Long team1 = m.getTeam1().getId();
        Long team2 = m.getTeam2().getId();
        int goals1 = 0;
        int goals2 = 0;
        for (Match other : matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(team1, team1)) {
            boolean sameTie = !other.getId().equals(m.getId())
                    && other.getCompetition().getId().equals(c.getId())
                    && (other.getTeam1().getId().equals(team2)
                            || other.getTeam2().getId().equals(team2))
                    && tour.equalsIgnoreCase(LEG_SUFFIX
                            .matcher(
                                    other.getRoundLabel() == null
                                            ? ""
                                            : other.getRoundLabel().trim())
                            .replaceAll("")
                            .trim());
            if (!sameTie || other.getScore1() == null || other.getScore2() == null) continue;
            boolean team1AtHome = other.getTeam1().getId().equals(team1);
            goals1 += team1AtHome ? other.getScore1() : other.getScore2();
            goals2 += team1AtHome ? other.getScore2() : other.getScore1();
        }
        boolean firstLeg = label.toUpperCase().matches(".*-\s*ALLER$");
        return new MatchDto.Knockout(!firstLeg, goals1, goals2);
    }

    /**
     * Pour chaque coupe d'Europe (LDC/EL/EC) des matchs donnes : rang de chaque equipe dans la
     * phase de ligue de cette coupe. Un match de coupe d'Europe affiche ce rang plutot que celui
     * du championnat national ; une coupe dont la phase de ligue n'a pas encore commence
     * (tours preliminaires) donne une table vide, donc aucun rang.
     */
    private Map<Long, Map<Long, Integer>> continentalRanks(List<Match> matches) {
        Map<Long, Map<Long, Integer>> byCompetition = new HashMap<>();
        for (Match m : matches) {
            Competition c = m.getCompetition();
            if (c.getType() != CompetitionType.CONTINENTAL_CUP || byCompetition.containsKey(c.getId())) continue;
            List<StandingRowDto> rows = standingsService.computeStandingsForRound(c.getId(), LEAGUE_PHASE_ROUND);
            Map<Long, Integer> ranks = new HashMap<>();
            for (int i = 0; i < rows.size(); i++) {
                ranks.putIfAbsent(rows.get(i).teamId(), i + 1);
            }
            byCompetition.put(c.getId(), ranks);
        }
        return byCompetition;
    }

    /**
     * Rang actuel de chaque equipe des matchs donnes dans le championnat de son pays (saison la
     * plus recente), utilise pour les matchs de championnat et de coupe nationale (et pour les
     * affiches, cf. FeaturedMatchService). Un championnat scinde
     * en mini-groupes garde le rang global (groupe du haut d'abord, cf. StandingsService). Une
     * equipe absente du classement (aucun match joue) n'a pas de rang.
     */
    private Map<Long, Integer> leagueRanks(List<Match> matches) {
        Set<String> countries = new HashSet<>();
        for (Match m : matches) {
            countries.add(m.getTeam1().getCountry());
            countries.add(m.getTeam2().getCountry());
        }
        Map<String, Competition> leagueByCountry = new HashMap<>();
        for (Competition c : competitionRepository.findByType(CompetitionType.LEAGUE)) {
            if (c.getCountry() == null || !countries.contains(c.getCountry())) continue;
            leagueByCountry.merge(c.getCountry(), c, (a, b) -> a.getSeason() >= b.getSeason() ? a : b);
        }
        Map<Long, Integer> ranks = new HashMap<>();
        for (Competition league : leagueByCountry.values()) {
            List<StandingRowDto> rows = standingsService.computeStandings(league.getId());
            for (int i = 0; i < rows.size(); i++) {
                ranks.putIfAbsent(rows.get(i).teamId(), i + 1);
            }
        }
        return ranks;
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

    /**
     * Calendrier international (selections nationales, toutes confederations) de la saison en cours :
     * la plus recente des competitions internationales, l'historique (V116 et suivantes) n'y figure pas.
     */
    @Transactional(readOnly = true)
    public List<MatchDto> findInternational() {
        return competitionRepository.findByType(CompetitionType.INTERNATIONAL).stream()
                .map(Competition::getSeason)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .map(season -> matchRepository
                        .findByCompetition_TypeAndCompetition_SeasonOrderByDateAscTimeAsc(
                                CompetitionType.INTERNATIONAL, season)
                        .stream()
                        .map(MatchDto::from)
                        .toList())
                .orElse(List.of());
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
        if (dto.team1Id().equals(dto.team2Id())) {
            throw new IllegalArgumentException("Un club ne peut pas s'affronter lui-meme");
        }
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
        // Les ecrans hors bracket n'envoient pas la position : on ne l'efface pas.
        if (dto.bracketPosition() != null) {
            match.setBracketPosition(dto.bracketPosition());
        }
    }

    /** Sans statut force : un match dont les deux scores sont saisis est termine, sinon a venir. */
    private static MatchStatus statusFromScores(MatchCreateDto dto) {
        return dto.score1() != null && dto.score2() != null ? MatchStatus.COMPLETED : MatchStatus.SCHEDULED;
    }
}
