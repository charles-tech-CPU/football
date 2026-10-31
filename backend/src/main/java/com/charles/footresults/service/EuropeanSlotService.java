package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.EuropeanSlot;
import com.charles.footresults.domain.FinalStanding;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.EuropeanSlotDto;
import com.charles.footresults.dto.EuropeanSlotUpdateDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.EuropeanSlotRepository;
import com.charles.footresults.repository.FinalStandingRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Places en coupes d'Europe de la saison suivante, pays par pays.
 *
 * Generation : faute de liste d'acces UEFA dans l'appli, chaque pays reprend les points d'entree
 * de ses clubs de la saison en cours (competition et premier tour joue par chacun, un club
 * reverse dans une coupe inferieure apres une elimination n'y est compte qu'a son entree). Les
 * places sont ensuite modifiables a la main (nouveau cycle UEFA, pays qui change de rang...).
 *
 * Remplissage (a chaque cloture d'un championnat ou d'une coupe nationale) : les places d'un pays
 * sont classees de la meilleure (LDC phase de ligue) a la moins bonne (Conference League 1er
 * tour) ; la meilleure place de Europa League (a defaut de Conference League) revient au
 * vainqueur de la coupe, les autres aux clubs du championnat dans l'ordre du classement fige. Si
 * le vainqueur de la coupe est deja qualifie par le championnat, sa place revient au club suivant
 * du classement. Une place dont le club a ete choisi a la main n'est jamais modifiee.
 */
@Service
@Transactional
public class EuropeanSlotService {

    static final List<String> COMPETITIONS = List.of("LDC", "EL", "EC");
    static final Map<Integer, String> STAGES = Map.of(
            1, "Phase de ligue",
            2, "Barrage",
            3, "3e tour de qualification",
            4, "Voie principale (2e / 3e tour)",
            5, "2e tour de qualification",
            6, "1er tour de qualification");
    static final String CHAMPION = "Champion";
    static final String CUP_WINNER = "Vainqueur de coupe";
    private static final String LEAGUE_PHASE = "PHASE DE LIGUE";

    private static final Comparator<EuropeanSlot> BEST_FIRST = Comparator.comparingInt(
                    (EuropeanSlot s) -> COMPETITIONS.indexOf(s.getCompetitionCode()))
            .thenComparing(EuropeanSlot::getEntryStage)
            .thenComparing(EuropeanSlot::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final EuropeanSlotRepository slotRepository;
    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;
    private final FinalStandingRepository finalStandingRepository;
    private final TeamRepository teamRepository;

    public EuropeanSlotService(
            EuropeanSlotRepository slotRepository,
            CompetitionRepository competitionRepository,
            MatchRepository matchRepository,
            FinalStandingRepository finalStandingRepository,
            TeamRepository teamRepository) {
        this.slotRepository = slotRepository;
        this.competitionRepository = competitionRepository;
        this.matchRepository = matchRepository;
        this.finalStandingRepository = finalStandingRepository;
        this.teamRepository = teamRepository;
    }

    /** Places d'une saison (la derniere generee par defaut), de la meilleure a la moins bonne, pays par pays. */
    @Transactional(readOnly = true)
    public List<EuropeanSlotDto> findBySeason(Integer season) {
        Integer target = season != null ? season : slotRepository.findLatestSeason();
        if (target == null) {
            return List.of();
        }
        return slotRepository.findBySeason(target).stream()
                .sorted(BEST_FIRST.thenComparing(EuropeanSlot::getCountry))
                .map(EuropeanSlotService::toDto)
                .toList();
    }

    /**
     * Cree les places de la saison "season" a partir des points d'entree des clubs dans les coupes
     * d'Europe de la saison precedente, puis les remplit d'apres les competitions deja cloturees.
     */
    public List<EuropeanSlotDto> generate(Integer season) {
        if (slotRepository.existsBySeason(season)) {
            throw new IllegalArgumentException("Les places de cette saison existent deja : modifie-les a la main");
        }
        Map<Long, int[]> entryByTeam = new HashMap<>();
        Map<Long, Team> teams = new HashMap<>();
        for (Competition c : competitionRepository.findByTypeAndSeason(CompetitionType.CONTINENTAL_CUP, season - 1)) {
            int competition = COMPETITIONS.indexOf(c.getCode());
            if (competition < 0) {
                continue;
            }
            List<Match> matches = matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(c.getId());
            LocalDate leaguePhaseStart = matches.stream()
                    .filter(m -> label(m).contains(LEAGUE_PHASE) && m.getDate() != null)
                    .map(Match::getDate)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            for (Match m : matches) {
                Integer stage = stageOf(m, leaguePhaseStart);
                if (stage == null) {
                    continue;
                }
                for (Team t : List.of(m.getTeam1(), m.getTeam2())) {
                    if (t.getCountry() == null || t.getName().toUpperCase().startsWith("A DETERMINER")) {
                        continue;
                    }
                    teams.put(t.getId(), t);
                    int[] entry = {competition, stage};
                    entryByTeam.merge(t.getId(), entry, (a, b) -> compareEntry(a, b) <= 0 ? a : b);
                }
            }
        }
        Set<String> countries = new HashSet<>();
        entryByTeam.forEach((teamId, entry) -> {
            EuropeanSlot slot = new EuropeanSlot();
            slot.setSeason(season);
            slot.setCompetitionCode(COMPETITIONS.get(entry[0]));
            slot.setEntryStage(entry[1]);
            slot.setCountry(teams.get(teamId).getCountry());
            slotRepository.save(slot);
            countries.add(slot.getCountry());
        });
        countries.forEach(country -> fill(season, country));
        return findBySeason(season);
    }

    /** Remplit les places de tous les pays d'une saison d'apres les competitions cloturees. */
    public List<EuropeanSlotDto> fillAll(Integer season) {
        slotRepository.findBySeason(season).stream()
                .map(EuropeanSlot::getCountry)
                .distinct()
                .forEach(country -> fill(season, country));
        return findBySeason(season);
    }

    /**
     * Remplit les places d'un pays pour la saison "season" d'apres son championnat et sa coupe de
     * la saison precedente, une fois cloturees (cf. CompetitionClosureService).
     */
    public void fill(Integer season, String country) {
        List<EuropeanSlot> slots = slotRepository.findBySeasonAndCountry(season, country).stream()
                .sorted(BEST_FIRST)
                .toList();
        if (slots.isEmpty()) {
            return;
        }
        Competition league = competitionRepository
                .findFirstByTypeAndCountryAndSeason(CompetitionType.LEAGUE, country, season - 1)
                .orElse(null);
        Competition cup = competitionRepository
                .findFirstByTypeAndCountryAndSeason(CompetitionType.DOMESTIC_CUP, country, season - 1)
                .orElse(null);
        List<Team> ranking = league != null && league.isClosed()
                ? finalStandingRepository.findByCompetitionIdOrderByFinalRankAsc(league.getId()).stream()
                        .map(FinalStanding::getTeam)
                        .toList()
                : List.of();
        Team cupWinner = cup != null && cup.isClosed() ? cup.getWinnerTeam() : null;

        List<EuropeanSlot> automatic = slots.stream().filter(s -> !s.isManual()).toList();
        Set<Long> alreadyQualified = new HashSet<>();
        slots.stream()
                .filter(EuropeanSlot::isManual)
                .map(EuropeanSlot::getTeam)
                .filter(Objects::nonNull)
                .forEach(t -> alreadyQualified.add(t.getId()));
        for (EuropeanSlot s : automatic) {
            s.setTeam(null);
            s.setRoute(null);
        }

        EuropeanSlot cupSlot = cup == null ? null : cupSlot(automatic);
        List<Team> leagueTeams = ranking.stream()
                .filter(t -> !alreadyQualified.contains(t.getId()))
                .toList();
        List<EuropeanSlot> leagueSlots = new ArrayList<>(automatic);
        boolean cupWinnerQualifiedByLeague = cupWinner != null
                && leagueTeams
                        .subList(0, Math.min(leagueTeams.size(), automatic.size() - (cupSlot == null ? 0 : 1)))
                        .stream()
                        .anyMatch(t -> t.getId().equals(cupWinner.getId()));
        if (cupSlot != null && !cupWinnerQualifiedByLeague) {
            leagueSlots.remove(cupSlot);
            cupSlot.setRoute(CUP_WINNER);
            if (cupWinner != null && !alreadyQualified.contains(cupWinner.getId())) {
                cupSlot.setTeam(cupWinner);
                alreadyQualified.add(cupWinner.getId());
            }
        }
        List<Team> remaining = ranking.stream()
                .filter(t -> !alreadyQualified.contains(t.getId()))
                .toList();
        for (int i = 0; i < leagueSlots.size() && i < remaining.size(); i++) {
            Team team = remaining.get(i);
            leagueSlots.get(i).setTeam(team);
            leagueSlots.get(i).setRoute(leagueRoute(ranking.indexOf(team) + 1));
        }
        slotRepository.saveAll(slots);
    }

    public EuropeanSlotDto create(EuropeanSlotUpdateDto dto) {
        EuropeanSlot slot = new EuropeanSlot();
        apply(slot, dto);
        return toDto(slotRepository.save(slot));
    }

    public EuropeanSlotDto update(Long id, EuropeanSlotUpdateDto dto) {
        EuropeanSlot slot = slotRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Place europeenne introuvable : " + id));
        apply(slot, dto);
        slotRepository.save(slot);
        if (!slot.isManual()) {
            fill(slot.getSeason(), slot.getCountry());
        }
        return toDto(slot);
    }

    public void delete(Long id) {
        slotRepository.deleteById(id);
    }

    private void apply(EuropeanSlot slot, EuropeanSlotUpdateDto dto) {
        if (!COMPETITIONS.contains(dto.competitionCode())) {
            throw new IllegalArgumentException("Competition attendue : LDC, EL ou EC");
        }
        slot.setSeason(dto.season());
        slot.setCompetitionCode(dto.competitionCode());
        slot.setEntryStage(dto.entryStage());
        slot.setCountry(dto.country());
        slot.setRoute(dto.route() == null || dto.route().isBlank() ? null : dto.route());
        slot.setTeam(
                dto.teamId() == null
                        ? null
                        : teamRepository
                                .findById(dto.teamId())
                                .orElseThrow(
                                        () -> new EntityNotFoundException("Equipe introuvable : " + dto.teamId())));
        slot.setManual(dto.teamId() != null);
    }

    /** Place reservee au vainqueur de la coupe : la meilleure de Europa League, a defaut de Conference League. */
    private static EuropeanSlot cupSlot(List<EuropeanSlot> bestFirst) {
        for (String code : List.of("EL", "EC")) {
            for (EuropeanSlot s : bestFirst) {
                if (code.equals(s.getCompetitionCode())) {
                    return s;
                }
            }
        }
        return null;
    }

    static String leagueRoute(int rank) {
        return rank == 1 ? CHAMPION : rank + "e du championnat";
    }

    /**
     * Ordre des points d'entree, le vrai d'abord : la competition la plus haute (un club elimine
     * n'est reverse que vers une coupe inferieure), puis dans celle-ci le premier tour joue.
     */
    private static int compareEntry(int[] a, int[] b) {
        return a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(b[1], a[1]);
    }

    private static String label(Match m) {
        return m.getRoundLabel() == null ? "" : m.getRoundLabel().toUpperCase();
    }

    /**
     * Tour d'entree correspondant a un match (cf. STAGES) ; null pour la phase finale (barrage
     * apres la phase de ligue, huitiemes...), qui n'est jamais un point d'entree.
     */
    static Integer stageOf(Match m, LocalDate leaguePhaseStart) {
        String label = label(m);
        if (label.contains(LEAGUE_PHASE)) {
            return 1;
        }
        if (label.contains("BARRAGE")) {
            boolean afterLeaguePhase = m.getDate() != null
                    && leaguePhaseStart != null
                    && !m.getDate().isBefore(leaguePhaseStart);
            return afterLeaguePhase ? null : 2;
        }
        if (label.contains("3E TOUR")) {
            return 3;
        }
        if (label.contains("VOIE PRINCIPALE")) {
            return 4;
        }
        if (label.contains("2E TOUR")) {
            return 5;
        }
        if (label.contains("1ER TOUR")) {
            return 6;
        }
        return null;
    }

    private static EuropeanSlotDto toDto(EuropeanSlot s) {
        Team t = s.getTeam();
        return new EuropeanSlotDto(
                s.getId(),
                s.getSeason(),
                s.getCompetitionCode(),
                s.getEntryStage(),
                STAGES.get(s.getEntryStage()),
                s.getCountry(),
                s.getRoute(),
                t == null ? null : t.getId(),
                t == null ? null : t.getName(),
                t == null ? null : t.getLogoPath(),
                s.isManual());
    }
}
