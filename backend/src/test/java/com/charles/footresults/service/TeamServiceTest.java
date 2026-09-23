package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.MatchRepository;
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
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamCompetitionStatusRepository statusRepository;

    @InjectMocks
    private TeamService teamService;

    private Team doublon;
    private Team vllaznia;
    private Team tirana;
    private Competition albanie;
    private Competition coupe;

    @BeforeEach
    void setUp() {
        doublon = team(1L, "VLLAZINA");
        vllaznia = team(2L, "VLLAZNIA");
        tirana = team(3L, "TIRANA");
        albanie = competition(10L);
        coupe = competition(11L);
    }

    @Test
    void fusionReaffecteLesMatchsEtLesStatutsPuisSupprimeLeDoublon() {
        givenTeamsExist();
        Match domicile = match(doublon, tirana);
        Match exterieur = match(tirana, doublon);
        when(matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(1L, 1L)).thenReturn(List.of(domicile, exterieur));
        TeamCompetitionStatus statutDejaPresent = new TeamCompetitionStatus(albanie, doublon);
        TeamCompetitionStatus statutAReprendre = new TeamCompetitionStatus(coupe, doublon);
        when(statusRepository.findByTeamId(1L)).thenReturn(List.of(statutDejaPresent, statutAReprendre));
        when(statusRepository.findByCompetitionIdAndTeamId(10L, 2L))
                .thenReturn(Optional.of(new TeamCompetitionStatus(albanie, vllaznia)));
        when(statusRepository.findByCompetitionIdAndTeamId(11L, 2L)).thenReturn(Optional.empty());

        TeamDto merged = teamService.merge(1L, 2L);

        assertThat(merged).isEqualTo(new TeamDto(2L, "VLLAZNIA", "Albanie", null));
        assertThat(domicile.getTeam1()).isSameAs(vllaznia);
        assertThat(domicile.getTeam2()).isSameAs(tirana);
        assertThat(exterieur.getTeam1()).isSameAs(tirana);
        assertThat(exterieur.getTeam2()).isSameAs(vllaznia);
        verify(matchRepository).saveAll(List.of(domicile, exterieur));
        verify(statusRepository).delete(statutDejaPresent);
        assertThat(statutAReprendre.getTeam()).isSameAs(vllaznia);
        verify(statusRepository).save(statutAReprendre);
        verify(teamRepository).delete(doublon);
    }

    @Test
    void impossibleDeFusionnerUneEquipeAvecElleMeme() {
        assertThatThrownBy(() -> teamService.merge(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("elle-meme");
        verifyNoInteractions(teamRepository, matchRepository, statusRepository);
    }

    @Test
    void deuxEquipesQuiSeSontAffronteesNeSontPasDesDoublons() {
        givenTeamsExist();
        when(matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(1L, 1L))
                .thenReturn(List.of(match(doublon, tirana), match(vllaznia, doublon)));

        assertThatThrownBy(() -> teamService.merge(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deja affrontees");
        verify(teamRepository, never()).delete(any());
    }

    @Test
    void affrontementDetecteAussiQuandLeDoublonRecoit() {
        givenTeamsExist();
        when(matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(1L, 1L))
                .thenReturn(List.of(match(doublon, vllaznia)));

        assertThatThrownBy(() -> teamService.merge(1L, 2L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equipeSourceInconnue() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.merge(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void equipeCibleInconnue() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(doublon));
        when(teamRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.merge(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("2");
    }

    private void givenTeamsExist() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(doublon));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(vllaznia));
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, "Albanie");
        team.setId(id);
        return team;
    }

    private static Competition competition(Long id) {
        Competition competition = new Competition("ALBANIE", "Albanie", CompetitionType.LEAGUE, "Albanie", 2027);
        competition.setId(id);
        return competition;
    }

    private static Match match(Team team1, Team team2) {
        Match match = new Match();
        match.setTeam1(team1);
        match.setTeam2(team2);
        return match;
    }
}
