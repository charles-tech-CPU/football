package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.EuropeanSlot;
import com.charles.footresults.domain.FinalStanding;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.EuropeanSlotDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.EuropeanSlotRepository;
import com.charles.footresults.repository.FinalStandingRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EuropeanSlotServiceTest {

    private static final int SEASON = 2028;

    @Mock
    private EuropeanSlotRepository slotRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private FinalStandingRepository finalStandingRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private EuropeanSlotService slotService;

    private final List<Team> clubs = IntStream.rangeClosed(1, 6)
            .mapToObj(i -> team((long) i, "CLUB " + i, "France"))
            .toList();
    private final List<EuropeanSlot> slots = new ArrayList<>();
    private Competition league;
    private Competition cup;

    @BeforeEach
    void setUp() {
        league = competition(1L, CompetitionType.LEAGUE);
        cup = competition(2L, CompetitionType.DOMESTIC_CUP);
        lenient().when(slotRepository.findBySeasonAndCountry(SEASON, "France")).thenReturn(slots);
        lenient().when(slotRepository.findBySeason(SEASON)).thenReturn(slots);
        lenient().when(slotRepository.save(any(EuropeanSlot.class))).thenAnswer(inv -> {
            EuropeanSlot s = inv.getArgument(0);
            s.setId((long) slots.size() + 1);
            slots.add(s);
            return s;
        });
        List<FinalStanding> standings = new ArrayList<>();
        for (int i = 0; i < clubs.size(); i++) {
            FinalStanding f = new FinalStanding();
            f.setTeam(clubs.get(i));
            f.setFinalRank(i + 1);
            standings.add(f);
        }
        lenient()
                .when(finalStandingRepository.findByCompetitionIdOrderByFinalRankAsc(1L))
                .thenReturn(standings);
    }

    @Test
    void leVainqueurDeCoupePrendLaMeilleurePlaceEuropaLeagueEtLeChampionnatLeReste() {
        givenSlots("EC", 4, "LDC", 1, "EL", 1, "LDC", 1);
        close(league, clubs.get(0));
        close(cup, clubs.get(5));

        slotService.fill(SEASON, "France");

        assertThat(filled())
                .containsExactly(
                        tuple("LDC", "CLUB 1", "Champion"),
                        tuple("LDC", "CLUB 2", "2e du championnat"),
                        tuple("EL", "CLUB 6", "Vainqueur de coupe"),
                        tuple("EC", "CLUB 3", "3e du championnat"));
    }

    @Test
    void unVainqueurDeCoupeDejaQualifieLaissePlaceAuSuivantDuClassement() {
        givenSlots("LDC", 1, "LDC", 1, "EL", 1, "EC", 4);
        close(league, clubs.get(0));
        close(cup, clubs.get(1));

        slotService.fill(SEASON, "France");

        assertThat(filled())
                .containsExactly(
                        tuple("LDC", "CLUB 1", "Champion"),
                        tuple("LDC", "CLUB 2", "2e du championnat"),
                        tuple("EL", "CLUB 3", "3e du championnat"),
                        tuple("EC", "CLUB 4", "4e du championnat"));
    }

    @Test
    void sansCoupeClotureeLaPlaceDeCoupeResteADeterminerEtUnChoixManuelEstGarde() {
        givenSlots("LDC", 1, "EL", 1, "EC", 4);
        slots.get(0).setTeam(clubs.get(4));
        slots.get(0).setManual(true);
        close(league, clubs.get(0));

        slotService.fill(SEASON, "France");

        assertThat(filled())
                .containsExactly(
                        tuple("LDC", "CLUB 5", null),
                        tuple("EL", null, "Vainqueur de coupe"),
                        tuple("EC", "CLUB 1", "Champion"));
    }

    @Test
    void lesPlacesSontGenereesAuPremierTourJoueParChaqueClub() {
        Team kups = team(10L, "KUPS", "Finlande");
        Team brann = team(11L, "BRANN", "Norvege");
        Competition ldc = continental(68L, "LDC");
        Competition ec = continental(70L, "EC");
        when(competitionRepository.findByTypeAndSeason(CompetitionType.CONTINENTAL_CUP, SEASON - 1))
                .thenReturn(List.of(ldc, ec));
        when(matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(68L))
                .thenReturn(List.of(
                        match("1ER TOUR QUALIF - Aller", kups, brann, null),
                        match("2E TOUR QUALIF - Aller", brann, kups, null),
                        match("PHASE DE LIGUE", brann, kups, LocalDate.of(2026, 9, 16)),
                        // Barrage apres la phase de ligue : jamais un point d'entree.
                        match("BARRAGE - Aller", brann, kups, LocalDate.of(2027, 2, 17))));
        when(matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(70L))
                .thenReturn(List.of(match("VOIE PRINCIPALE - Aller", kups, brann, null)));

        List<EuropeanSlotDto> generated = slotService.generate(SEASON);

        assertThat(generated)
                .extracting(EuropeanSlotDto::country, EuropeanSlotDto::competitionCode, EuropeanSlotDto::entryRound)
                .containsExactlyInAnyOrder(
                        tuple("Finlande", "LDC", "1er tour de qualification"),
                        tuple("Norvege", "LDC", "1er tour de qualification"));
    }

    private List<org.assertj.core.groups.Tuple> filled() {
        return slots.stream()
                .sorted((a, b) -> a.getCompetitionCode().equals(b.getCompetitionCode())
                        ? Long.compare(a.getId(), b.getId())
                        : Integer.compare(
                                EuropeanSlotService.COMPETITIONS.indexOf(a.getCompetitionCode()),
                                EuropeanSlotService.COMPETITIONS.indexOf(b.getCompetitionCode())))
                .map(s -> tuple(
                        s.getCompetitionCode(),
                        s.getTeam() == null ? null : s.getTeam().getName(),
                        s.getRoute()))
                .toList();
    }

    /** Places (competition, tour) dans l'ordre de saisie. */
    private void givenSlots(Object... codeAndStage) {
        for (int i = 0; i < codeAndStage.length; i += 2) {
            EuropeanSlot s = new EuropeanSlot();
            s.setId((long) slots.size() + 1);
            s.setSeason(SEASON);
            s.setCountry("France");
            s.setCompetitionCode((String) codeAndStage[i]);
            s.setEntryStage((Integer) codeAndStage[i + 1]);
            slots.add(s);
        }
    }

    private static void close(Competition c, Team winner) {
        c.setClosedAt(LocalDateTime.now());
        c.setWinnerTeam(winner);
    }

    private Competition competition(Long id, CompetitionType type) {
        Competition c = new Competition("FRANCE", "France", type, "France", SEASON - 1);
        c.setId(id);
        lenient()
                .when(competitionRepository.findFirstByTypeAndCountryAndSeason(type, "France", SEASON - 1))
                .thenReturn(Optional.of(c));
        return c;
    }

    private static Competition continental(Long id, String code) {
        Competition c = new Competition(code, code, CompetitionType.CONTINENTAL_CUP, null, SEASON - 1);
        c.setId(id);
        return c;
    }

    private static Match match(String round, Team team1, Team team2, LocalDate date) {
        Match m = new Match();
        m.setRoundLabel(round);
        m.setTeam1(team1);
        m.setTeam2(team2);
        m.setDate(date);
        return m;
    }

    private static Team team(Long id, String name, String country) {
        Team t = new Team(name, country);
        t.setId(id);
        return t;
    }
}
