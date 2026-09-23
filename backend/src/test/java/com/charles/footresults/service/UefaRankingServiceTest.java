package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.ClubUefaRanking;
import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.CountryUefaRanking;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.ClubUefaRankingDto;
import com.charles.footresults.dto.CountryUefaRankingDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.ClubUefaRankingRepository;
import com.charles.footresults.repository.CountryUefaRankingRepository;
import com.charles.footresults.repository.MatchRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UefaRankingServiceTest {

    private static final LocalDate LEAGUE_PHASE_START = LocalDate.of(2026, 9, 16);

    @Mock
    private ClubUefaRankingRepository clubRepository;

    @Mock
    private CountryUefaRankingRepository countryRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private StandingsService standingsService;

    @InjectMocks
    private UefaRankingService uefaRankingService;

    private Competition ldc;
    private Team psg;
    private Team inter;
    private Team celtic;
    private Team benfica;

    @BeforeEach
    void setUp() {
        ldc = new Competition("LDC", "Ligue des Champions", CompetitionType.CONTINENTAL_CUP, null, 2027);
        ldc.setId(100L);
        psg = team(1L, "PSG");
        inter = team(2L, "Inter");
        celtic = team(3L, "Celtic");
        benfica = team(4L, "Benfica");
    }

    // --- Bareme des points ---

    @Test
    void tourDeQualificationVictoireUnPointNulUnDemiPoint() {
        givenMatches(
                played("2E TOUR QUALIF - Aller", date(7, 22), psg, inter, 2, 0),
                played("2E TOUR QUALIF - Retour", date(7, 29), inter, psg, 1, 1));

        Map<String, BigDecimal> points = points2027(psg, inter);

        assertThat(points.get("PSG")).isEqualByComparingTo("1.5");
        assertThat(points.get("Inter")).isEqualByComparingTo("0.5");
    }

    @Test
    void voiePrincipaleCompteCommeUnTourDeQualification() {
        givenMatches(played("VOIE PRINCIPALE 3E TOUR", date(8, 5), psg, inter, 1, 0));

        assertThat(points2027(psg, inter).get("PSG")).isEqualByComparingTo("1");
    }

    @Test
    void phaseDeLigueVictoireDeuxPointsNulUnPoint() {
        givenMatches(
                played("Phase de ligue", date(9, 16), psg, inter, 3, 1),
                played("Phase de ligue", date(9, 30), psg, celtic, 0, 0));

        Map<String, BigDecimal> points = points2027(psg, inter, celtic);

        assertThat(points.get("PSG")).isEqualByComparingTo("3");
        assertThat(points.get("Inter")).isEqualByComparingTo("0");
        assertThat(points.get("Celtic")).isEqualByComparingTo("1");
    }

    @Test
    void phasesFinalesVictoireDeuxPoints() {
        givenMatches(
                played("SEIZIEME DE FINALE", date(2, 10), psg, inter, 1, 0),
                played("HUITIEME DE FINALE - Aller", date(3, 3), psg, celtic, 1, 0),
                played("HUITEME DE FINALE - Retour", date(3, 10), celtic, psg, 0, 1),
                played("QUART DE FINALE", date(4, 7), psg, benfica, 2, 0),
                played("DEMI-FINALE", date(4, 28), psg, inter, 2, 1),
                played("FINALE", date(5, 30), psg, celtic, 1, 0));

        assertThat(points2027(psg).get("PSG")).isEqualByComparingTo("12");
    }

    @Test
    void barrageAvantLaPhaseDeLigueRapporteLeBaremeDesQualifications() {
        givenMatches(
                played("BARRAGE - Aller", date(8, 19), psg, inter, 1, 0),
                played("Phase de ligue", LEAGUE_PHASE_START, celtic, benfica, 0, 0));

        assertThat(points2027(psg).get("PSG")).isEqualByComparingTo("1");
    }

    @Test
    void barrageApresLeDebutDeLaPhaseDeLigueRapporteLeBaremePrincipal() {
        givenMatches(
                played("Phase de ligue", LEAGUE_PHASE_START.plusWeeks(2), celtic, benfica, 0, 0),
                played("Phase de ligue", LEAGUE_PHASE_START, celtic, inter, 0, 0),
                played("BARRAGE - Aller", date(2, 17), psg, inter, 0, 0));

        assertThat(points2027(psg).get("PSG")).isEqualByComparingTo("1");
    }

    @Test
    void barrageSansPhaseDeLigueDateeEstConsidereCommeAvantLaPhaseDeLigue() {
        Match undatedLeague = played("Phase de ligue", null, celtic, benfica, 0, 0);
        givenMatches(undatedLeague, played("BARRAGE", date(2, 17), psg, inter, 1, 0));

        assertThat(points2027(psg).get("PSG")).isEqualByComparingTo("1");
    }

    @Test
    void unTourInconnuOuUnMatchNonJoueNeRapporteRien() {
        Match scheduled = match("Phase de ligue", date(10, 1), psg, celtic, null, null, MatchStatus.SCHEDULED);
        Match postponed = match("Phase de ligue", date(10, 1), psg, benfica, 1, 0, MatchStatus.POSTPONED);
        givenMatches(played("Match amical", date(7, 1), psg, inter, 5, 0), scheduled, postponed);

        assertThat(points2027(psg).get("PSG")).isEqualByComparingTo("0");
    }

    @Test
    void unTourSansLibelleNeRapporteRien() {
        givenMatches(played(null, date(7, 1), psg, inter, 5, 0));

        assertThat(points2027(psg).get("PSG")).isEqualByComparingTo("0");
    }

    // --- Elimination en confrontation directe ---

    @Test
    void egaliteALAggregatSansTirsAuButPersonneNEstElimine() {
        givenMatches(
                played("HUITIEME DE FINALE - Aller", date(3, 3), psg, inter, 2, 1),
                played("HUITIEME DE FINALE - Retour", date(3, 10), inter, psg, 1, 0));

        assertThat(liveCups(psg, inter)).containsEntry("PSG", "LDC").containsEntry("Inter", "LDC");
    }

    @Test
    void equipeBattueSurLesDeuxManchesPerdSaCoupeEnCours() {
        // La manche retour est saisie avant l'aller : le tri par date doit les remettre dans l'ordre.
        givenMatches(
                played("QUART DE FINALE - Retour", date(4, 14), inter, psg, 0, 2),
                played("QUART DE FINALE - Aller", date(4, 7), psg, inter, 1, 1));

        assertThat(liveCups(psg, inter)).containsEntry("PSG", "LDC").containsEntry("Inter", null);
    }

    @Test
    void equipeQuiRecoitAuMatchAllerEtPerdEstElimineeAussi() {
        givenMatches(
                played("QUART DE FINALE - Aller", date(4, 7), inter, psg, 0, 3),
                played("QUART DE FINALE - Retour", date(4, 14), psg, inter, 0, 1));

        assertThat(liveCups(psg, inter)).containsEntry("PSG", "LDC").containsEntry("Inter", null);
    }

    @Test
    void egaliteALAggregatLesTirsAuButDuMatchRetourDesignentLePerdant() {
        Match retour = played("DEMI-FINALE - Retour", date(5, 5), inter, psg, 1, 0);
        retour.setPenaltyScore1(4);
        retour.setPenaltyScore2(5);
        givenMatches(played("DEMI-FINALE - Aller", date(4, 28), psg, inter, 1, 0), retour);

        assertThat(liveCups(psg, inter)).containsEntry("PSG", "LDC").containsEntry("Inter", null);
    }

    @Test
    void tirsAuButGagnesParLEquipeQuiRecoitAuRetour() {
        Match retour = played("DEMI-FINALE - Retour", date(5, 5), inter, psg, 1, 0);
        retour.setPenaltyScore1(5);
        retour.setPenaltyScore2(3);
        givenMatches(played("DEMI-FINALE - Aller", date(4, 28), psg, inter, 1, 0), retour);

        assertThat(liveCups(psg, inter)).containsEntry("PSG", null).containsEntry("Inter", "LDC");
    }

    @Test
    void finaleSurUnSeulMatchAuxTirsAuBut() {
        Match finale = played("FINALE", date(5, 30), psg, inter, 0, 0);
        finale.setPenaltyScore1(3);
        finale.setPenaltyScore2(4);
        givenMatches(finale);

        assertThat(liveCups(psg, inter)).containsEntry("PSG", null).containsEntry("Inter", "LDC");
    }

    @Test
    void egaliteSansTirsAuButSaisisPersonneNEstElimine() {
        Match avecUnSeulTab = played("FINALE", date(5, 30), celtic, benfica, 0, 0);
        avecUnSeulTab.setPenaltyScore1(3);
        Match tabEgaux = played("QUART DE FINALE", date(4, 7), psg, inter, 1, 1);
        tabEgaux.setPenaltyScore1(4);
        tabEgaux.setPenaltyScore2(4);
        givenMatches(avecUnSeulTab, tabEgaux);

        assertThat(liveCups(psg, inter, celtic, benfica))
                .allSatisfy((name, cup) -> assertThat(cup).isEqualTo("LDC"));
    }

    @Test
    void confrontationPasEncoreTermineeNElimineePersonne() {
        givenMatches(
                played("1ER TOUR QUALIF - Aller", date(7, 8), psg, inter, 3, 0),
                match("1ER TOUR QUALIF - Retour", date(7, 15), inter, psg, null, null, MatchStatus.SCHEDULED),
                played("2E TOUR QUALIF - Aller", date(7, 22), celtic, benfica, 3, 0),
                match("2E TOUR QUALIF - Retour", date(7, 29), benfica, celtic, 1, null, MatchStatus.COMPLETED),
                played("3E TOUR QUALIF - Aller", date(8, 5), inter, benfica, 3, 0),
                match("3E TOUR QUALIF - Retour", date(8, 12), benfica, inter, null, 1, MatchStatus.COMPLETED));

        assertThat(liveCups(psg, inter, celtic, benfica))
                .allSatisfy((name, cup) -> assertThat(cup).isEqualTo("LDC"));
    }

    // --- Elimination en phase de ligue ---

    @Test
    void phaseDeLigueTermineeLesClubsAuDelaDu24eRangSontElimines() {
        givenMatches(played("Phase de ligue", LEAGUE_PHASE_START, psg, inter, 1, 0));
        List<StandingRowDto> standings = new ArrayList<>(
                IntStream.rangeClosed(1, 24).mapToObj(i -> row(1000L + i)).toList());
        standings.add(row(inter.getId())); // 25e
        standings.add(0, row(psg.getId())); // 1er (pousse tout le monde d'un rang)
        when(standingsService.computeStandingsForRound(ldc.getId(), "PHASE DE LIGUE"))
                .thenReturn(standings);

        assertThat(liveCups(psg, inter)).containsEntry("PSG", "LDC").containsEntry("Inter", null);
    }

    @Test
    void phaseDeLigueEnCoursPersonneNEstElimineEtLeClassementNEstPasCalcule() {
        givenMatches(
                played("Phase de ligue", LEAGUE_PHASE_START, psg, inter, 1, 0),
                match(
                        "Phase de ligue",
                        LEAGUE_PHASE_START.plusWeeks(1),
                        inter,
                        celtic,
                        null,
                        null,
                        MatchStatus.SCHEDULED));

        assertThat(liveCups(psg, inter)).containsEntry("PSG", "LDC").containsEntry("Inter", "LDC");
        verify(standingsService, never()).computeStandingsForRound(anyLong(), any());
    }

    // --- Classement des clubs ---

    @Test
    void clubSansEquipeLieeNAPasDePointsNiDeLogo() {
        givenMatches();
        ClubUefaRanking orphan = club(null, "Club disparu", "France", "EL");
        orphan.setUefaRank(42);
        orphan.setPoints2026(new BigDecimal("12.5"));
        when(clubRepository.findAllByOrderByUefaRankAsc()).thenReturn(List.of(orphan));

        ClubUefaRankingDto dto = uefaRankingService.findClubRankings().get(0);

        assertThat(dto.teamId()).isNull();
        assertThat(dto.teamLogoPath()).isNull();
        assertThat(dto.rank()).isEqualTo(42);
        assertThat(dto.clubName()).isEqualTo("Club disparu");
        assertThat(dto.points2027()).isEqualByComparingTo("0");
        assertThat(dto.points2026()).isEqualByComparingTo("12.5");
        assertThat(dto.currentCup()).isEqualTo("EL");
    }

    @Test
    void clubSansCoupeEnCoursNAPasDeBadge() {
        givenMatches();
        when(clubRepository.findAllByOrderByUefaRankAsc()).thenReturn(List.of(club(psg, "PSG", "France", null)));

        ClubUefaRankingDto dto = uefaRankingService.findClubRankings().get(0);

        assertThat(dto.currentCup()).isNull();
        assertThat(dto.teamId()).isEqualTo(psg.getId());
        assertThat(dto.teamLogoPath()).isEqualTo("1.png");
    }

    // --- Classement des pays ---

    @Test
    void lesPointsEtLesClubsEnCourseSontCumulesParPays() {
        givenMatches(
                played("Phase de ligue", LEAGUE_PHASE_START, psg, inter, 2, 0),
                played("Phase de ligue", LEAGUE_PHASE_START, celtic, benfica, 1, 1));
        Team lens = team(5L, "Lens");
        Team rennes = team(6L, "Rennes");
        when(clubRepository.findAll())
                .thenReturn(List.of(
                        club(psg, "PSG", "France", "LDC"),
                        club(lens, "Lens", "France", "EL"),
                        club(rennes, "Rennes", "France", "EC"),
                        club(null, "Club sans equipe", "France", "EC"),
                        club(inter, "Inter", "Italie", "LDC"),
                        club(celtic, "Celtic", null, "LDC"),
                        club(benfica, "Benfica", "Portugal", "AUTRE")));
        when(countryRepository.findAllByOrderByUefaRankAsc())
                .thenReturn(List.of(country("France"), country("Italie"), country("Portugal"), country("Suisse")));

        Map<String, CountryUefaRankingDto> byCountry = uefaRankingService.findCountryRankings().stream()
                .collect(Collectors.toMap(CountryUefaRankingDto::country, Function.identity()));

        CountryUefaRankingDto france = byCountry.get("France");
        assertThat(france.points2027()).isEqualByComparingTo("2");
        assertThat(Arrays.asList(france.ldcNow(), france.elNow(), france.ecNow()))
                .containsExactly(1, 1, 2);
        assertThat(france.colorCode()).isEqualTo("BLUE");
        assertThat(byCountry.get("Italie").points2027()).isEqualByComparingTo("0");
        assertThat(byCountry.get("Portugal").points2027()).isEqualByComparingTo("1");
        assertThat(byCountry.get("Portugal").colorCode()).isEqualTo("RED");
        assertThat(byCountry.get("Suisse").ldcNow()).isZero();
        assertThat(byCountry.get("Suisse").colorCode()).isEqualTo("RED");
    }

    @Test
    void couleurDuPaysSelonLaMeilleureCoupeEncoreJouee() {
        givenMatches();
        when(clubRepository.findAll())
                .thenReturn(List.of(club(psg, "PSG", "France", "EL"), club(inter, "Inter", "Italie", "EC")));
        CountryUefaRanking france = country("France");
        france.setUefaRank(2);
        france.setLdcDebut(4);
        france.setNb2027(7);
        when(countryRepository.findAllByOrderByUefaRankAsc()).thenReturn(List.of(france, country("Italie")));

        List<CountryUefaRankingDto> countries = uefaRankingService.findCountryRankings();

        assertThat(countries).extracting(CountryUefaRankingDto::colorCode).containsExactly("ORANGE", "YELLOW");
        assertThat(countries.get(0).rank()).isEqualTo(2);
        assertThat(countries.get(0).ldcDebut()).isEqualTo(4);
        assertThat(countries.get(0).nb2027()).isEqualTo(7);
    }

    @Test
    void unClubEliminePerdSaPlaceDansLesClubsEnCourseDeSonPays() {
        givenMatches(played("FINALE", date(5, 30), psg, inter, 0, 1));
        when(clubRepository.findAll())
                .thenReturn(List.of(club(psg, "PSG", "France", "LDC"), club(inter, "Inter", "Italie", "LDC")));
        when(countryRepository.findAllByOrderByUefaRankAsc()).thenReturn(List.of(country("France"), country("Italie")));

        List<CountryUefaRankingDto> countries = uefaRankingService.findCountryRankings();

        assertThat(countries.get(0).ldcNow()).isZero();
        assertThat(countries.get(0).colorCode()).isEqualTo("RED");
        assertThat(countries.get(1).ldcNow()).isEqualTo(1);
    }

    // --- Outils ---

    private void givenMatches(Match... matches) {
        when(matchRepository.findByCompetition_CodeIn(List.of("LDC", "EL", "EC")))
                .thenReturn(new ArrayList<>(List.of(matches)));
    }

    /** Points 2027 recalcules, par nom de club, via le classement des clubs. */
    private Map<String, BigDecimal> points2027(Team... teams) {
        when(clubRepository.findAllByOrderByUefaRankAsc())
                .thenReturn(Arrays.stream(teams)
                        .map(t -> club(t, t.getName(), "France", "LDC"))
                        .toList());
        return uefaRankingService.findClubRankings().stream()
                .collect(Collectors.toMap(ClubUefaRankingDto::clubName, ClubUefaRankingDto::points2027));
    }

    /** Coupe en cours (null si elimine), par nom de club : tous inscrits en LDC au depart. */
    private Map<String, String> liveCups(Team... teams) {
        when(clubRepository.findAllByOrderByUefaRankAsc())
                .thenReturn(Arrays.stream(teams)
                        .map(t -> club(t, t.getName(), "France", "LDC"))
                        .toList());
        Map<String, String> cups = new java.util.HashMap<>();
        uefaRankingService.findClubRankings().forEach(dto -> cups.put(dto.clubName(), dto.currentCup()));
        return cups;
    }

    private static LocalDate date(int month, int day) {
        return LocalDate.of(month >= 7 ? 2026 : 2027, month, day);
    }

    private Match played(String round, LocalDate date, Team team1, Team team2, int score1, int score2) {
        return match(round, date, team1, team2, score1, score2, MatchStatus.COMPLETED);
    }

    private Match match(
            String round, LocalDate date, Team team1, Team team2, Integer score1, Integer score2, MatchStatus status) {
        Match match = new Match();
        match.setCompetition(ldc);
        match.setRoundLabel(round);
        match.setDate(date);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setScore1(score1);
        match.setScore2(score2);
        match.setStatus(status);
        return match;
    }

    private static StandingRowDto row(Long teamId) {
        return new StandingRowDto(teamId, "Equipe " + teamId, null, null, 8, 0, 0, 0, 0, 0, 0, 0, null);
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, null);
        team.setId(id);
        team.setLogoPath(id + ".png");
        return team;
    }

    private static ClubUefaRanking club(Team team, String name, String country, String currentCup) {
        ClubUefaRanking club = new ClubUefaRanking();
        club.setTeam(team);
        club.setClubName(name);
        club.setCountry(country);
        club.setCurrentCup(currentCup);
        return club;
    }

    private static CountryUefaRanking country(String name) {
        CountryUefaRanking country = new CountryUefaRanking();
        country.setCountry(name);
        return country;
    }
}
