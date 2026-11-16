package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.StandingRowDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchHighlightServiceTest {

    @Mock
    private StandingsService standingsService;

    @Mock
    private RivalryService rivalryService;

    @InjectMocks
    private MatchHighlightService service;

    @Test
    void lesEquipesMenaceesSontEnZoneRougeOuATroisPointsAuPlus() {
        // 6 equipes, 2 places en zone rouge : 1re place de la zone a 20 points -> menace jusqu'a 23.
        List<StandingRowDto> rows = List.of(row(1, 50), row(2, 40), row(3, 24), row(4, 23), row(5, 20), row(6, 15));

        assertThat(MatchHighlightService.threatened(rows, 2)).containsExactlyInAnyOrder(4L, 5L, 6L);
        assertThat(MatchHighlightService.threatened(rows, 0)).isEmpty();
    }

    @Test
    void unMatchDeRelegationSeJoueDansLesCinqDernieresJourneesEntreEquipesMenacees() {
        Competition league = league();
        when(standingsService.computeStandings(1L))
                .thenReturn(List.of(row(1, 50), row(2, 40), row(3, 24), row(4, 23), row(5, 20), row(6, 15)));
        Match battle = match(100L, league, "J26", 4, 6);
        Match notThreatened = match(101L, league, "J26", 3, 5);
        Match tooEarly = match(102L, league, "J25", 4, 6);

        assertThat(service.relegationBattleIds(List.of(battle, notThreatened, tooEarly)))
                .containsExactly(100L);
    }

    @Test
    void uneRivaliteEstReconnueQuelQueSoitLeTerrain() {
        Competition league = league();
        when(rivalryService.labelsByPair()).thenReturn(Map.of(RivalryService.pairKey(4L, 6L), "Le Classique"));

        assertThat(service.rivalryLabels(List.of(match(100L, league, "J3", 6, 4), match(101L, league, "J3", 1, 2))))
                .containsExactly(Map.entry(100L, "Le Classique"));
    }

    private static Competition league() {
        Competition c = new Competition("FRA", "Ligue 1", CompetitionType.LEAGUE, "France", 2027);
        c.setId(1L);
        c.setTotalRounds(30);
        c.setRelegationSlots(1);
        c.setBarrageSlots(1);
        return c;
    }

    private static Match match(Long id, Competition c, String round, long team1, long team2) {
        Match m = new Match();
        m.setId(id);
        m.setCompetition(c);
        m.setRoundLabel(round);
        m.setTeam1(team(team1));
        m.setTeam2(team(team2));
        return m;
    }

    private static Team team(long id) {
        Team t = new Team();
        t.setId(id);
        t.setName("T" + id);
        return t;
    }

    private static StandingRowDto row(long teamId, int points) {
        return new StandingRowDto(teamId, "T" + teamId, null, "France", 0, 0, 0, 0, 0, 0, 0, points, null);
    }
}
