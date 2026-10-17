package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.InternationalRankingRowDto;
import com.charles.footresults.repository.MatchRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternationalRankingServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private InternationalRankingService service;

    private Team france;
    private Team espagne;
    private Team bresil;
    private Competition euroQualifs;
    private Competition copa;
    private Competition coupeDuMonde;
    private long nextId = 1;

    @BeforeEach
    void setUp() {
        france = team(1L, "France");
        espagne = team(2L, "Espagne");
        bresil = team(3L, "Bresil");
        euroQualifs = competition("UEFA_EUROQ_2024");
        copa = competition("CONMEBOL_COPA_2024");
        coupeDuMonde = competition("FIFA_WC_2026");
    }

    @Test
    void noteDeDepartSelonLaConfederationDesCompetitionsContinentales() {
        givenMatches(
                played(euroQualifs, day(1), france, espagne, 1, 1, true),
                played(copa, day(2), bresil, team(4L, "Chili"), 0, 0, true));

        List<InternationalRankingRowDto> rows = service.compute();

        InternationalRankingRowDto br = row(rows, "Bresil");
        InternationalRankingRowDto fr = row(rows, "France");
        assertThat(br.confederation()).isEqualTo("CONMEBOL");
        assertThat(br.startRating()).isEqualTo(1700);
        assertThat(fr.confederation()).isEqualTo("UEFA");
        // Nul entre deux equipes de meme note sur terrain neutre : aucun point echange
        assertThat(fr.rating()).isEqualTo(1650);
    }

    @Test
    void lAvantageDuTerrainRendLaVictoireADomicileMoinsRentable() {
        givenMatches(played(euroQualifs, day(1), france, espagne, 1, 0, false));
        int domicile = row(service.compute(), "France").rating();

        givenMatches(played(euroQualifs, day(1), france, espagne, 1, 0, true));
        int neutre = row(service.compute(), "France").rating();

        assertThat(domicile).isLessThan(neutre);
        // K = 40, attendu 0,5 sur terrain neutre : +20
        assertThat(neutre).isEqualTo(1670);
    }

    @Test
    void lImportanceDuMatchEtLEcartDeButsAugmententLeGain() {
        assertThat(InternationalRankingService.k(match(coupeDuMonde))).isEqualTo(60);
        assertThat(InternationalRankingService.k(match(competition("UEFA_EURO_2024"))))
                .isEqualTo(50);
        assertThat(InternationalRankingService.k(match(competition("UEFA_EUROQ_2024"))))
                .isEqualTo(40);
        assertThat(InternationalRankingService.k(match(competition("AFC_ASIANCUPQ_2027"))))
                .isEqualTo(40);
        assertThat(InternationalRankingService.goalMultiplier(1)).isEqualTo(1);
        assertThat(InternationalRankingService.goalMultiplier(2)).isEqualTo(1.5);
        assertThat(InternationalRankingService.goalMultiplier(3)).isCloseTo(1.75, within(1e-9));
        assertThat(InternationalRankingService.goalMultiplier(5)).isCloseTo(2.0, within(1e-9));
    }

    @Test
    void classementBilanFormeEtMeilleureVictoire() {
        givenMatches(
                played(euroQualifs, day(1), france, espagne, 2, 0, true),
                played(euroQualifs, day(5), espagne, france, 1, 1, true),
                played(coupeDuMonde, day(9), bresil, france, 3, 1, true));

        List<InternationalRankingRowDto> rows = service.compute();

        assertThat(rows).extracting(InternationalRankingRowDto::rank).containsExactly(1, 2, 3);
        InternationalRankingRowDto fr = row(rows, "France");
        assertThat(fr.played()).isEqualTo(3);
        assertThat(fr.wins()).isEqualTo(1);
        assertThat(fr.draws()).isEqualTo(1);
        assertThat(fr.losses()).isEqualTo(1);
        assertThat(fr.form()).containsExactly("W", "D", "L");
        assertThat(fr.bestWin().opponentName()).isEqualTo("Espagne");
        assertThat(fr.bestWin().score()).isEqualTo("2-0");
        assertThat(row(rows, "Bresil").bestWin().opponentName()).isEqualTo("France");
    }

    @Test
    void aucunMatchJoue() {
        givenMatches();

        assertThat(service.compute()).isEmpty();
    }

    private void givenMatches(Match... matches) {
        when(matchRepository.findByCompetition_TypeAndStatus(CompetitionType.INTERNATIONAL, MatchStatus.COMPLETED))
                .thenReturn(List.of(matches));
    }

    private static InternationalRankingRowDto row(List<InternationalRankingRowDto> rows, String name) {
        return rows.stream().filter(r -> r.teamName().equals(name)).findFirst().orElseThrow();
    }

    private Match played(Competition c, LocalDate date, Team team1, Team team2, int s1, int s2, boolean neutral) {
        Match m = match(c);
        m.setDate(date);
        m.setTeam1(team1);
        m.setTeam2(team2);
        m.setScore1(s1);
        m.setScore2(s2);
        m.setNeutral(neutral);
        m.setStatus(MatchStatus.COMPLETED);
        return m;
    }

    private Match match(Competition c) {
        Match m = new Match();
        m.setId(nextId++);
        m.setCompetition(c);
        m.setRoundLabel("J1");
        return m;
    }

    private static LocalDate day(int d) {
        return LocalDate.of(2026, 9, d);
    }

    private static Competition competition(String code) {
        return new Competition(code, code, CompetitionType.INTERNATIONAL, null, 2026);
    }

    private static Team team(Long id, String name) {
        Team t = new Team(name, name);
        t.setId(id);
        return t;
    }
}
