package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.FinalStanding;
import com.charles.footresults.domain.Honour;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.ClosureDto;
import com.charles.footresults.dto.ClosureRequestDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.FinalStandingRepository;
import com.charles.footresults.repository.HonourRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cloture de fin de saison d'un championnat, d'une coupe nationale ou d'une coupe d'Europe : le classement final (dans
 * l'ordre valide a la main, departages au tete-a-tete et mini-championnats compris) est fige, la
 * saison est ajoutee au palmares (ou mise a jour si elle y est deja, ex: import Wikipedia) et les
 * matchs de la competition passent en lecture seule (cf. MatchService) jusqu'a sa reouverture. Les
 * places europeennes de la saison suivante du pays sont remplies au passage (cf. EuropeanSlotService).
 */
@Service
@Transactional
public class CompetitionClosureService {

    static final String LEAGUE_KIND = "LEAGUE";
    static final String CUP_KIND = "CUP";
    private static final String LEAGUE_LABEL = "Championnat";
    private static final String CUP_LABEL = "Coupe nationale";
    private static final String EUROPE = "Europe";
    /** Coupes d'Europe : code de la competition -> type et libelle du palmares (cf. V125). */
    private static final Map<String, String[]> EUROPE_KINDS = Map.of(
            "LDC", new String[] {"LDC", "Ligue des Champions"},
            "EL", new String[] {"EL", "Europa League"},
            "EC", new String[] {"ECL", "Conference League"});

    private static final Pattern SEASON = Pattern.compile("^\\d{4}(-\\d{4})?$");

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final FinalStandingRepository finalStandingRepository;
    private final HonourRepository honourRepository;
    private final StandingsService standingsService;
    private final EuropeanSlotService europeanSlotService;

    public CompetitionClosureService(
            CompetitionRepository competitionRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            FinalStandingRepository finalStandingRepository,
            HonourRepository honourRepository,
            StandingsService standingsService,
            EuropeanSlotService europeanSlotService) {
        this.competitionRepository = competitionRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.finalStandingRepository = finalStandingRepository;
        this.honourRepository = honourRepository;
        this.standingsService = standingsService;
        this.europeanSlotService = europeanSlotService;
    }

    @Transactional(readOnly = true)
    public ClosureDto closure(Long competitionId) {
        Competition competition = competition(competitionId);
        List<StandingRowDto> frozen =
                finalStandingRepository.findByCompetitionIdOrderByFinalRankAsc(competitionId).stream()
                        .map(StandingsService::frozenRow)
                        .toList();
        return new ClosureDto(competitionId, competition.getClosedAt(), suggestedSeason(competition), frozen);
    }

    public ClosureDto close(Long competitionId, ClosureRequestDto dto) {
        Competition competition = competition(competitionId);
        if (!SEASON.matcher(dto.season()).matches()) {
            throw new IllegalArgumentException("Saison attendue au format 2026 ou 2026-2027 : " + dto.season());
        }
        Honour honour;
        if (competition.getType() == CompetitionType.LEAGUE && competition.getCountry() != null) {
            List<Team> podium = freezeStandings(competition, dto.ranking());
            honour = honour(competition.getCountry(), LEAGUE_KIND, LEAGUE_LABEL, dto.season());
            setClubs(
                    honour,
                    podium.get(0),
                    podium.size() > 1 ? podium.get(1) : null,
                    podium.size() > 2 ? podium.get(2) : null);
        } else if (competition.getType() == CompetitionType.DOMESTIC_CUP && competition.getCountry() != null) {
            honour = honour(competition.getCountry(), CUP_KIND, CUP_LABEL, dto.season());
            setFinalists(honour, dto);
        } else if (competition.getType() == CompetitionType.CONTINENTAL_CUP
                && EUROPE_KINDS.containsKey(competition.getCode())) {
            String[] kindAndLabel = EUROPE_KINDS.get(competition.getCode());
            honour = honour(EUROPE, kindAndLabel[0], kindAndLabel[1], dto.season());
            setFinalists(honour, dto);
            honour.setWinnerCountry(honour.getWinnerTeam().getCountry());
            honour.setRunnerUpCountry(
                    honour.getRunnerUpTeam() == null
                            ? null
                            : honour.getRunnerUpTeam().getCountry());
        } else {
            throw new IllegalArgumentException(
                    "Seuls un championnat, une coupe nationale ou une coupe d'Europe (LDC, EL, EC) se cloturent");
        }
        honourRepository.save(honour);
        competition.setClosedAt(LocalDateTime.now());
        competition.setWinnerTeam(honour.getWinnerTeam());
        competitionRepository.save(competition);
        if (competition.getCountry() != null) {
            europeanSlotService.fill(competition.getSeason() + 1, competition.getCountry());
        }
        return closure(competitionId);
    }

    /** Coupe (nationale ou d'Europe) : vainqueur obligatoire, finaliste facultatif. */
    private void setFinalists(Honour honour, ClosureRequestDto dto) {
        if (dto.winnerId() == null) {
            throw new IllegalArgumentException("Vainqueur de la coupe manquant");
        }
        if (dto.winnerId().equals(dto.runnerUpId())) {
            throw new IllegalArgumentException("Le vainqueur et le finaliste doivent etre differents");
        }
        setClubs(honour, team(dto.winnerId()), dto.runnerUpId() == null ? null : team(dto.runnerUpId()), null);
    }

    /**
     * Rouvre une competition cloturee (correction d'un resultat...) : le classement fige est supprime,
     * la ligne de palmares est gardee et sera mise a jour a la prochaine cloture.
     */
    public ClosureDto reopen(Long competitionId) {
        Competition competition = competition(competitionId);
        finalStandingRepository.deleteByCompetitionId(competitionId);
        competition.setClosedAt(null);
        competition.setWinnerTeam(null);
        competitionRepository.save(competition);
        if (competition.getCountry() != null) {
            europeanSlotService.fill(competition.getSeason() + 1, competition.getCountry());
        }
        return closure(competitionId);
    }

    /** Fige le classement dans l'ordre donne, qui doit reprendre toutes les equipes classees ; renvoie le podium. */
    private List<Team> freezeStandings(Competition competition, List<Long> ranking) {
        List<StandingRowDto> live = standingsService.computeLiveStandings(competition.getId());
        Map<Long, StandingRowDto> byTeam =
                live.stream().collect(Collectors.toMap(StandingRowDto::teamId, Function.identity()));
        if (ranking == null || ranking.size() != live.size() || !new HashSet<>(ranking).equals(byTeam.keySet())) {
            throw new IllegalArgumentException("Le classement final doit reprendre une fois chacune des " + live.size()
                    + " equipes du championnat");
        }
        finalStandingRepository.deleteByCompetitionId(competition.getId());
        List<Team> teams = ranking.stream().map(this::team).toList();
        for (int i = 0; i < teams.size(); i++) {
            StandingRowDto row = byTeam.get(teams.get(i).getId());
            FinalStanding f = new FinalStanding();
            f.setCompetition(competition);
            f.setTeam(teams.get(i));
            f.setFinalRank(i + 1);
            f.setGroupName(row.group());
            f.setPlayed(row.played());
            f.setWon(row.won());
            f.setDrawn(row.drawn());
            f.setLost(row.lost());
            f.setGoalsFor(row.goalsFor());
            f.setGoalsAgainst(row.goalsAgainst());
            f.setPoints(row.points());
            finalStandingRepository.save(f);
        }
        return teams.subList(0, Math.min(3, teams.size()));
    }

    /** Ligne de palmares de la saison : existante (ex: deja importee de Wikipedia) ou nouvelle. */
    private Honour honour(String scope, String kind, String label, String season) {
        return honourRepository
                .findFirstByScopeAndKindAndSeason(scope, kind, season)
                .orElseGet(() -> {
                    Honour h = new Honour();
                    h.setScope(scope);
                    h.setKind(kind);
                    h.setCompetitionLabel(label);
                    h.setSeason(season);
                    return h;
                });
    }

    private void setClubs(Honour honour, Team winner, Team runnerUp, Team third) {
        honour.setSeasonEnd(
                Integer.parseInt(honour.getSeason().substring(honour.getSeason().length() - 4)));
        honour.setWinner(honourName(winner));
        honour.setWinnerTeam(winner);
        honour.setRunnerUp(runnerUp == null ? null : honourName(runnerUp));
        honour.setRunnerUpTeam(runnerUp);
        honour.setThird(third == null ? null : honourName(third));
        honour.setThirdTeam(third);
    }

    /**
     * Nom du club tel qu'il figure deja au palmares ("KuPS" plutot que "KUPS") : le decompte des
     * titres regroupe les saisons par nom. A defaut, le nom du club dans l'appli.
     */
    private String honourName(Team team) {
        for (Honour h : honourRepository.findByTeam(team.getId())) {
            if (sameTeam(h.getWinnerTeam(), team)) {
                return h.getWinner();
            }
            if (sameTeam(h.getRunnerUpTeam(), team)) {
                return h.getRunnerUp();
            }
            if (sameTeam(h.getThirdTeam(), team)) {
                return h.getThird();
            }
        }
        return team.getName();
    }

    private static boolean sameTeam(Team a, Team b) {
        return a != null && Objects.equals(a.getId(), b.getId());
    }

    /**
     * Saison proposee pour le palmares, au format deja utilise par ce palmares ("2026" pour un
     * championnat sur l'annee civile, "2026-2027" sinon) et terminee l'annee du dernier match.
     */
    String suggestedSeason(Competition competition) {
        List<Integer> years = matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(competition.getId()).stream()
                .map(Match::getDate)
                .filter(Objects::nonNull)
                .map(LocalDate::getYear)
                .toList();
        if (years.isEmpty()) {
            return String.valueOf(competition.getSeason());
        }
        int first = years.stream().min(Integer::compare).orElseThrow();
        int last = years.stream().max(Integer::compare).orElseThrow();
        String kind = competition.getType() == CompetitionType.LEAGUE ? LEAGUE_KIND : CUP_KIND;
        // Coupes d'Europe : toujours sur deux annees ("2026-2027").
        boolean spansTwoYears = competition.getCountry() == null
                || honourRepository
                        .findFirstByScopeAndKindOrderBySeasonEndDesc(competition.getCountry(), kind)
                        .map(h -> h.getSeason().contains("-"))
                        .orElse(first != last);
        return spansTwoYears ? (last - 1) + "-" + last : String.valueOf(last);
    }

    private Competition competition(Long id) {
        return competitionRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + id));
    }

    private Team team(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
    }
}
