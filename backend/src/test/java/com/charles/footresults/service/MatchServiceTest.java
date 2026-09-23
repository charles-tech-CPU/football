package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchCreateDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.MatchPageDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    private static final List<MatchStatus> NOT_UPCOMING =
            List.of(MatchStatus.COMPLETED, MatchStatus.POSTPONED, MatchStatus.SUSPENDED);

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private MatchService matchService;

    private Competition ligue1;
    private Team psg;
    private Team om;

    @BeforeEach
    void setUp() {
        ligue1 = new Competition("FRANCE", "France - Championnat 2027", CompetitionType.LEAGUE, "France", 2027);
        ligue1.setId(10L);
        psg = team(1L, "PSG");
        om = team(2L, "OM");
    }

    @Test
    void creationAvecScoresDonneUnMatchTermine() {
        givenReferencesExist();
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchDto created = matchService.create(dto(2, 1, null));

        assertThat(created.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(created.competitionCode()).isEqualTo("FRANCE");
        assertThat(created.team1Name()).isEqualTo("PSG");
        assertThat(created.team2Name()).isEqualTo("OM");
        assertThat(created.score1()).isEqualTo(2);
        assertThat(created.score2()).isEqualTo(1);
        assertThat(created.roundLabel()).isEqualTo("J1");
    }

    @Test
    void creationSansScoreDonneUnMatchAVenir() {
        givenReferencesExist();
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchDto created = matchService.create(dto(null, null, null));

        assertThat(created.status()).isEqualTo(MatchStatus.SCHEDULED);
    }

    @Test
    void creationAvecUnSeulScoreDonneUnMatchAVenir() {
        givenReferencesExist();
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchDto created = matchService.create(dto(1, null, null));

        assertThat(created.status()).isEqualTo(MatchStatus.SCHEDULED);
    }

    @Test
    void unStatutExplicitePrimeSurLesScores() {
        givenReferencesExist();
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchDto created = matchService.create(dto(null, null, MatchStatus.POSTPONED));

        assertThat(created.status()).isEqualTo(MatchStatus.POSTPONED);
    }

    @Test
    void miseAJourDUnMatchExistant() {
        Match existing = new Match();
        existing.setId(5L);
        when(matchRepository.findById(5L)).thenReturn(Optional.of(existing));
        givenReferencesExist();
        when(matchRepository.save(existing)).thenReturn(existing);

        MatchDto updated = matchService.update(5L, dto(0, 0, null));

        assertThat(updated.id()).isEqualTo(5L);
        assertThat(updated.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(existing.getScore1()).isZero();
    }

    @Test
    void miseAJourDUnMatchInexistantEchoue() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        MatchCreateDto dto = dto(1, 0, null);

        assertThatThrownBy(() -> matchService.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
        verify(matchRepository, never()).save(any());
    }

    @Test
    void creationAvecUneEquipeInconnueEchoue() {
        when(competitionRepository.findById(ligue1.getId())).thenReturn(Optional.of(ligue1));
        when(teamRepository.findById(psg.getId())).thenReturn(Optional.of(psg));
        when(teamRepository.findById(om.getId())).thenReturn(Optional.empty());
        MatchCreateDto dto = dto(1, 0, null);

        assertThatThrownBy(() -> matchService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Equipe introuvable");
        verify(matchRepository, never()).save(any());
    }

    @Test
    void creationAvecUneCompetitionInconnueEchoue() {
        when(competitionRepository.findById(ligue1.getId())).thenReturn(Optional.empty());
        MatchCreateDto dto = dto(1, 0, null);

        assertThatThrownBy(() -> matchService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Competition introuvable");
    }

    @Test
    void suppressionDelegueAuRepository() {
        matchService.delete(7L);

        verify(matchRepository).deleteById(7L);
    }

    @ParameterizedTest
    @CsvSource(
            nullValues = "NULL",
            value = {
                "NULL, NULL, NULL",
                "all, NULL, NULL",
                "league, LEAGUE, NULL",
                "cup, DOMESTIC_CUP, NULL",
                "ldc, NULL, LDC",
                "el, NULL, EL",
                "ec, NULL, EC"
            })
    void calendrierFiltreParTypeOuParCodeDeCompetition(
            String filter, CompetitionType expectedType, String expectedCode) {
        Match upcoming = match();
        when(matchRepository.findUpcomingFiltered(NOT_UPCOMING, expectedType, expectedCode, PageRequest.of(2, 50)))
                .thenReturn(new PageImpl<>(List.of(upcoming), PageRequest.of(2, 50), 101));

        MatchPageDto page = matchService.findUpcoming(2, 50, filter);

        assertThat(page.items()).hasSize(1);
        assertThat(page.totalCount()).isEqualTo(101);
    }

    @Test
    void lesListesDeMatchsSontConvertiesEnDto() {
        when(matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(10L)).thenReturn(List.of(match()));
        when(matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(1L, 1L)).thenReturn(List.of(match()));
        when(matchRepository.findRecentByStatus(MatchStatus.COMPLETED, PageRequest.of(0, 5)))
                .thenReturn(List.of(match()));
        when(matchRepository.findByStatusInOrderByDateAscTimeAsc(List.of(MatchStatus.POSTPONED, MatchStatus.SUSPENDED)))
                .thenReturn(List.of(match()));
        when(matchRepository.findByCompetition_TypeOrderByDateAscTimeAsc(CompetitionType.INTERNATIONAL))
                .thenReturn(List.of(match()));

        assertThat(matchService.findByCompetition(10L))
                .extracting(MatchDto::team1Name)
                .containsExactly("PSG");
        assertThat(matchService.findByTeam(1L)).hasSize(1);
        assertThat(matchService.findRecentResults(5)).hasSize(1);
        assertThat(matchService.findPostponedOrSuspended()).hasSize(1);
        assertThat(matchService.findInternational()).hasSize(1);
    }

    private void givenReferencesExist() {
        when(competitionRepository.findById(ligue1.getId())).thenReturn(Optional.of(ligue1));
        when(teamRepository.findById(psg.getId())).thenReturn(Optional.of(psg));
        when(teamRepository.findById(om.getId())).thenReturn(Optional.of(om));
    }

    private MatchCreateDto dto(Integer score1, Integer score2, MatchStatus status) {
        return new MatchCreateDto(
                ligue1.getId(),
                "J1",
                LocalDate.of(2026, 8, 15),
                LocalTime.of(21, 0),
                psg.getId(),
                om.getId(),
                score1,
                score2,
                null,
                null,
                status);
    }

    private Match match() {
        Match match = new Match();
        match.setCompetition(ligue1);
        match.setRoundLabel("J1");
        match.setTeam1(psg);
        match.setTeam2(om);
        return match;
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, "France");
        team.setId(id);
        return team;
    }
}
