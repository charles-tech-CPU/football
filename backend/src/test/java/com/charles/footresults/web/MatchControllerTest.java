package com.charles.footresults.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.charles.footresults.dto.MatchCreateDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.MatchPageDto;
import com.charles.footresults.service.MatchService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private final List<MatchDto> matches = List.of(Mockito.mock(MatchDto.class));

    @Test
    void matchsDUneCompetition() {
        when(matchService.findByCompetition(1L)).thenReturn(matches);

        assertThat(matchController.find(1L, 3L)).isSameAs(matches);
    }

    @Test
    void matchsDUneEquipe() {
        when(matchService.findByTeam(3L)).thenReturn(matches);

        assertThat(matchController.find(null, 3L)).isSameAs(matches);
    }

    @Test
    void sansCompetitionNiEquipeLaRequeteEstRefusee() {
        assertThatThrownBy(() -> matchController.find(null, null)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(matchService);
    }

    @Test
    void listesDeMatchs() {
        MatchPageDto page = new MatchPageDto(matches, 1);
        when(matchService.findRecentResults(300)).thenReturn(matches);
        when(matchService.findUpcoming(0, 20, "league")).thenReturn(page);
        when(matchService.findPostponedOrSuspended()).thenReturn(matches);
        when(matchService.findInternational()).thenReturn(matches);

        assertThat(matchController.recent(300)).isSameAs(matches);
        assertThat(matchController.upcoming(0, 20, "league")).isSameAs(page);
        assertThat(matchController.postponed()).isSameAs(matches);
        assertThat(matchController.international()).isSameAs(matches);
    }

    @Test
    void creationModificationSuppressionDeleguesAuService() {
        MatchCreateDto dto = new MatchCreateDto(1L, "J1", null, null, 1L, 2L, null, null, null, null, null);
        MatchDto created = matches.get(0);
        when(matchService.create(dto)).thenReturn(created);
        when(matchService.update(5L, dto)).thenReturn(created);

        assertThat(matchController.create(dto)).isSameAs(created);
        assertThat(matchController.update(5L, dto)).isSameAs(created);
        matchController.delete(5L);

        verify(matchService).delete(5L);
    }
}
