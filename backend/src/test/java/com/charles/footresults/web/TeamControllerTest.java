package com.charles.footresults.web;

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
import com.charles.footresults.dto.TeamCreateDto;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import com.charles.footresults.service.TeamService;
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
class TeamControllerTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private TeamCompetitionStatusRepository statusRepository;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private Team psg;
    private Team om;
    private Team inter;

    @BeforeEach
    void setUp() {
        psg = team(1L, "PSG", "France");
        om = team(2L, "OM", "France");
        inter = team(3L, "Inter", "Italie");
    }

    @Test
    void sansFiltreToutesLesEquipesTrieesParNom() {
        when(teamRepository.findAll()).thenReturn(List.of(psg, inter, om));

        assertThat(teamController.findAll(null, null)).extracting(TeamDto::name).containsExactly("Inter", "OM", "PSG");
    }

    @Test
    void filtreParPaysSansTenirCompteDeLaCasse() {
        when(teamRepository.findAll()).thenReturn(List.of(psg, inter, om));

        assertThat(teamController.findAll("FRANCE", null))
                .extracting(TeamDto::name)
                .containsExactly("OM", "PSG");
    }

    @Test
    void competitionNationaleLesEquipesDuPaysDeLaCompetition() {
        when(teamRepository.findAll()).thenReturn(List.of(psg, inter, om));
        when(competitionRepository.findById(10L)).thenReturn(Optional.of(competition(10L, "Italie")));

        assertThat(teamController.findAll("France", 10L))
                .extracting(TeamDto::name)
                .containsExactly("Inter");
    }

    @Test
    void competitionContinentaleLeRosterOfficielQuandIlEstConnu() {
        Competition ldc = competition(20L, null);
        when(teamRepository.findAll()).thenReturn(List.of(psg, inter, om));
        when(competitionRepository.findById(20L)).thenReturn(Optional.of(ldc));
        when(statusRepository.findByCompetitionId(20L))
                .thenReturn(List.of(new TeamCompetitionStatus(ldc, psg), new TeamCompetitionStatus(ldc, inter)));

        assertThat(teamController.findAll(null, 20L)).extracting(TeamDto::name).containsExactly("Inter", "PSG");
    }

    @Test
    void competitionContinentaleSansRosterLesEquipesAyantDejaJoue() {
        when(teamRepository.findAll()).thenReturn(List.of(psg, inter, om));
        when(competitionRepository.findById(20L)).thenReturn(Optional.of(competition(20L, null)));
        when(statusRepository.findByCompetitionId(20L)).thenReturn(List.of());
        when(teamRepository.findByCompetitionId(20L)).thenReturn(List.of(om));

        assertThat(teamController.findAll(null, 20L)).extracting(TeamDto::name).containsExactly("OM");
    }

    @Test
    void competitionInconnue() {
        when(teamRepository.findAll()).thenReturn(List.of());
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamController.findAll(null, 99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findOneRenvoieLEquipe() {
        psg.setLogoPath("1.png");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(psg));

        assertThat(teamController.findOne(1L)).isEqualTo(new TeamDto(1L, "PSG", "France", "1.png"));
    }

    @Test
    void uneEquipeInconnueLeveUneErreurEnLectureCommeEnModification() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
        TeamCreateDto dto = new TeamCreateDto("PSG", "France");

        assertThatThrownBy(() -> teamController.findOne(99L)).isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> teamController.update(99L, dto)).isInstanceOf(EntityNotFoundException.class);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void creation() {
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team saved = inv.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        assertThat(teamController.create(new TeamCreateDto("Lens", "France")))
                .isEqualTo(new TeamDto(5L, "Lens", "France", null));
    }

    @Test
    void modification() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(psg));
        when(teamRepository.save(psg)).thenReturn(psg);

        TeamDto updated = teamController.update(1L, new TeamCreateDto("Paris SG", "France"));

        assertThat(updated.name()).isEqualTo("Paris SG");
    }

    @Test
    void suppression() {
        teamController.delete(1L);

        verify(teamRepository).deleteById(1L);
    }

    @Test
    void fusionDelegueAuService() {
        TeamDto merged = new TeamDto(2L, "OM", "France", null);
        when(teamService.merge(1L, 2L)).thenReturn(merged);

        assertThat(teamController.merge(1L, 2L)).isSameAs(merged);
    }

    private static Team team(Long id, String name, String country) {
        Team team = new Team(name, country);
        team.setId(id);
        return team;
    }

    private static Competition competition(Long id, String country) {
        CompetitionType type = country == null ? CompetitionType.CONTINENTAL_CUP : CompetitionType.LEAGUE;
        Competition competition = new Competition("TEST", "Competition de test", type, country, 2027);
        competition.setId(id);
        return competition;
    }
}
