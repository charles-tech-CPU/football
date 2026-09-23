package com.charles.footresults.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.charles.footresults.dto.ClubUefaRankingDto;
import com.charles.footresults.dto.CountryUefaRankingDto;
import com.charles.footresults.dto.HeadToHeadCellDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.dto.TeamStatusDto;
import com.charles.footresults.dto.TeamStatusUpdateDto;
import com.charles.footresults.service.StandingsService;
import com.charles.footresults.service.TeamStatusService;
import com.charles.footresults.service.UefaRankingService;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Controleurs qui se contentent de deleguer a un service : on verifie juste le branchement. */
class ReadOnlyControllersTest {

    private final StandingsService standingsService = mock(StandingsService.class);
    private final TeamStatusService teamStatusService = mock(TeamStatusService.class);
    private final UefaRankingService uefaRankingService = mock(UefaRankingService.class);

    @Test
    void classementGeneralOuDUnePhase() {
        List<StandingRowDto> general = List.of(mock(StandingRowDto.class));
        List<StandingRowDto> phase = List.of(mock(StandingRowDto.class));
        when(standingsService.computeStandings(1L)).thenReturn(general);
        when(standingsService.computeStandingsForRound(1L, "PHASE DE LIGUE")).thenReturn(phase);
        StandingsController controller = new StandingsController(standingsService);

        assertThat(controller.standings(1L, null)).isSameAs(general);
        assertThat(controller.standings(1L, "  ")).isSameAs(general);
        assertThat(controller.standings(1L, "PHASE DE LIGUE")).isSameAs(phase);
    }

    @Test
    void teteATete() {
        List<HeadToHeadCellDto> cells = List.of(new HeadToHeadCellDto(1L, 2L, 1, 0, 0));
        when(standingsService.computeHeadToHead(1L)).thenReturn(cells);

        assertThat(new StandingsController(standingsService).headToHead(1L)).isSameAs(cells);
    }

    @Test
    void statutsDesEquipes() {
        TeamStatusUpdateDto update = new TeamStatusUpdateDto(true, false, false, null, null);
        TeamStatusDto status = new TeamStatusDto(3L, true, false, false, null, null);
        when(teamStatusService.findByCompetition(1L)).thenReturn(List.of(status));
        when(teamStatusService.upsert(1L, 3L, update)).thenReturn(status);
        TeamStatusController controller = new TeamStatusController(teamStatusService);

        assertThat(controller.findByCompetition(1L)).containsExactly(status);
        assertThat(controller.upsert(1L, 3L, update)).isSameAs(status);
    }

    @Test
    void classementsUefa() {
        List<ClubUefaRankingDto> clubs = List.of(mock(ClubUefaRankingDto.class));
        List<CountryUefaRankingDto> countries = List.of(mock(CountryUefaRankingDto.class));
        when(uefaRankingService.findClubRankings()).thenReturn(clubs);
        when(uefaRankingService.findCountryRankings()).thenReturn(countries);
        UefaRankingController controller = new UefaRankingController(uefaRankingService);

        assertThat(controller.clubs()).isSameAs(clubs);
        assertThat(controller.countries()).isSameAs(countries);
    }
}
