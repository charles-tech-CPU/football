package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamStatusDto;
import com.charles.footresults.dto.TeamStatusUpdateDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamStatusServiceTest {

    private static final TeamStatusUpdateDto CHAMPION_EN_TITRE =
            new TeamStatusUpdateDto(true, false, true, "EL", "Championnat");

    @Mock
    private TeamCompetitionStatusRepository statusRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamStatusService teamStatusService;

    private Competition ligue1;
    private Team psg;

    @BeforeEach
    void setUp() {
        ligue1 = new Competition("FRANCE", "France - Championnat 2027", CompetitionType.LEAGUE, "France", 2027);
        ligue1.setId(10L);
        psg = new Team("PSG", "France");
        psg.setId(1L);
    }

    @Test
    void statutsDUneCompetition() {
        TeamCompetitionStatus status = new TeamCompetitionStatus(ligue1, psg);
        status.setPromoted(true);
        when(statusRepository.findByCompetitionId(10L)).thenReturn(List.of(status));

        assertThat(teamStatusService.findByCompetition(10L))
                .containsExactly(new TeamStatusDto(1L, false, true, false, null, null));
    }

    @Test
    void metAJourUnStatutExistant() {
        TeamCompetitionStatus existing = new TeamCompetitionStatus(ligue1, psg);
        existing.setPromoted(true);
        when(statusRepository.findByCompetitionIdAndTeamId(10L, 1L)).thenReturn(Optional.of(existing));
        when(statusRepository.save(existing)).thenReturn(existing);

        TeamStatusDto dto = teamStatusService.upsert(10L, 1L, CHAMPION_EN_TITRE);

        assertThat(dto).isEqualTo(new TeamStatusDto(1L, true, false, true, "EL", "Championnat"));
        verify(competitionRepository, never()).findById(any());
    }

    @Test
    void creeLeStatutSIlNExistePasEncore() {
        when(statusRepository.findByCompetitionIdAndTeamId(10L, 1L)).thenReturn(Optional.empty());
        when(competitionRepository.findById(10L)).thenReturn(Optional.of(ligue1));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(psg));
        when(statusRepository.save(any(TeamCompetitionStatus.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamStatusDto dto = teamStatusService.upsert(10L, 1L, CHAMPION_EN_TITRE);

        assertThat(dto.teamId()).isEqualTo(1L);
        assertThat(dto.defendingChampion()).isTrue();
        assertThat(dto.groupName()).isEqualTo("Championnat");
    }

    @Test
    void competitionInconnue() {
        when(statusRepository.findByCompetitionIdAndTeamId(99L, 1L)).thenReturn(Optional.empty());
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamStatusService.upsert(99L, 1L, CHAMPION_EN_TITRE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Competition introuvable");
    }

    @Test
    void equipeInconnue() {
        when(statusRepository.findByCompetitionIdAndTeamId(10L, 99L)).thenReturn(Optional.empty());
        when(competitionRepository.findById(10L)).thenReturn(Optional.of(ligue1));
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamStatusService.upsert(10L, 99L, CHAMPION_EN_TITRE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Equipe introuvable");
        verify(statusRepository, never()).save(any());
    }
}
