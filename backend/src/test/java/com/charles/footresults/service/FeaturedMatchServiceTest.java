package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.ClubUefaRanking;
import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.CountryUefaRanking;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.repository.ClubUefaRankingRepository;
import com.charles.footresults.repository.CountryUefaRankingRepository;
import com.charles.footresults.repository.MatchRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeaturedMatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private CountryUefaRankingRepository countryRankingRepository;

    @Mock
    private ClubUefaRankingRepository clubRankingRepository;

    @InjectMocks
    private FeaturedMatchService service;

    /** Rang en championnat = id de l'equipe. */
    private final Map<Long, Integer> leagueRanks = new HashMap<>();

    @Test
    void deuxAffichesParJourneeDansUnDesDixPremiersChampionnats() {
        givenTopCountries("France");
        Competition ligue1 = competition(1L, "FRANCE", CompetitionType.LEAGUE, "France");
        // 10 v 9 (19), 1 v 4 (5), 2 v 3 (5, moins bon rang 3 < 4), 5 v 8 (13).
        List<Match> round = List.of(
                match(100L, ligue1, 10, 9),
                match(101L, ligue1, 1, 4),
                match(102L, ligue1, 2, 3),
                match(103L, ligue1, 5, 8));
        givenRound(ligue1, round);

        assertThat(service.featuredMatchIds(List.of(round.get(0)), leagueRanks)).containsExactlyInAnyOrder(102L, 101L);
    }

    @Test
    void uneSeuleAfficheDansLesAutresChampionnats() {
        givenTopCountries("France");
        Competition malte = competition(2L, "MALTE", CompetitionType.LEAGUE, "Malte");
        List<Match> round = List.of(match(200L, malte, 1, 4), match(201L, malte, 2, 3), match(202L, malte, 5, 6));
        givenRound(malte, round);

        assertThat(service.featuredMatchIds(round, leagueRanks)).containsExactly(201L);
    }

    @Test
    void unMatchReporteOuUneEquipeSansClassementNestJamaisUneAffiche() {
        givenTopCountries("France");
        Competition malte = competition(2L, "MALTE", CompetitionType.LEAGUE, "Malte");
        Match reporte = match(200L, malte, 1, 2);
        reporte.setStatus(MatchStatus.POSTPONED);
        Match sansClassement = match(201L, malte, 3, 4);
        leagueRanks.remove(4L);
        Match affiche = match(202L, malte, 5, 6);
        givenRound(malte, List.of(reporte, sansClassement, affiche));

        assertThat(service.featuredMatchIds(List.of(affiche), leagueRanks)).containsExactly(202L);
    }

    @Test
    void quatreAffichesParTourEnCoupeDEuropeSelonLeClassementUefaDesClubs() {
        Competition ldc = competition(3L, "LDC", CompetitionType.CONTINENTAL_CUP, null);
        List<Match> round = new ArrayList<>();
        IntStream.range(0, 6).forEach(i -> round.add(match(300L + i, ldc, 20 + 2 * i, 21 + 2 * i)));
        givenRound(ldc, round);
        // Rang UEFA inverse des ids : les derniers matchs du tour sont les meilleures affiches ;
        // l'equipe 20 n'est pas classee UEFA.
        List<ClubUefaRanking> uefa = new ArrayList<>();
        for (long id = 21; id <= 31; id++) {
            uefa.add(clubRanking(id, (int) (40 - id)));
        }
        when(clubRankingRepository.findAll()).thenReturn(uefa);

        assertThat(service.featuredMatchIds(List.of(round.get(0)), leagueRanks))
                .containsExactlyInAnyOrder(305L, 304L, 303L, 302L);
    }

    @Test
    void pasDAfficheEnCoupeNationale() {
        Competition coupe = competition(4L, "FRANCE_CUP", CompetitionType.DOMESTIC_CUP, "France");

        assertThat(service.featuredMatchIds(List.of(match(400L, coupe, 1, 2)), leagueRanks))
                .isEmpty();
    }

    private void givenTopCountries(String... countries) {
        List<CountryUefaRanking> rankings = new ArrayList<>();
        for (String country : countries) {
            CountryUefaRanking r = new CountryUefaRanking();
            r.setCountry(country);
            rankings.add(r);
        }
        when(countryRankingRepository.findAllByOrderByUefaRankAsc()).thenReturn(rankings);
    }

    private void givenRound(Competition competition, List<Match> round) {
        when(matchRepository.findByCompetitionIdAndRoundLabel(competition.getId(), "J1"))
                .thenReturn(round);
    }

    private static Competition competition(Long id, String code, CompetitionType type, String country) {
        Competition c = new Competition(code, code, type, country, 2027);
        c.setId(id);
        return c;
    }

    private Match match(Long id, Competition competition, long team1, long team2) {
        Match m = new Match();
        m.setId(id);
        m.setCompetition(competition);
        m.setRoundLabel("J1");
        m.setStatus(MatchStatus.SCHEDULED);
        m.setTeam1(team(team1));
        m.setTeam2(team(team2));
        leagueRanks.put(team1, (int) team1);
        leagueRanks.put(team2, (int) team2);
        return m;
    }

    private static Team team(long id) {
        Team t = new Team("Equipe " + id, "France");
        t.setId(id);
        return t;
    }

    private static ClubUefaRanking clubRanking(long teamId, int rank) {
        ClubUefaRanking r = new ClubUefaRanking();
        r.setTeam(team(teamId));
        r.setUefaRank(rank);
        return r;
    }
}
