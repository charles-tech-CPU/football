package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.CompetitionDto;
import com.charles.footresults.dto.NextSeasonRequestDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NextSeasonServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamCompetitionStatusRepository statusRepository;

    @InjectMocks
    private NextSeasonService nextSeasonService;

    private Competition league;
    private Competition cup;
    private Team kups;
    private Team hjk;
    private Team promoted;
    private final List<Competition> saved = new ArrayList<>();
    private final List<TeamCompetitionStatus> statuses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        kups = team(10L, "KUPS");
        hjk = team(11L, "HJK");
        promoted = team(12L, "TPS");
        league = new Competition("FINLANDE", "Finlande - Championnat 2027", CompetitionType.LEAGUE, "Finlande", 2027);
        league.setId(1L);
        league.setLdcSlots(1);
        league.setRelegationSlots(1);
        league.setTotalRounds(27);
        league.setClosedAt(LocalDateTime.now());
        league.setWinnerTeam(kups);
        cup = new Competition("FINLANDE", "Finlande - Coupe 2027", CompetitionType.DOMESTIC_CUP, "Finlande", 2027);
        cup.setClosedAt(LocalDateTime.now());
        cup.setWinnerTeam(hjk);
        lenient().when(competitionRepository.findById(1L)).thenReturn(Optional.of(league));
        lenient()
                .when(competitionRepository.findFirstByTypeAndCountryAndSeason(
                        CompetitionType.DOMESTIC_CUP, "Finlande", 2027))
                .thenReturn(Optional.of(cup));
        lenient().when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> {
            saved.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        lenient().when(statusRepository.save(any(TeamCompetitionStatus.class))).thenAnswer(inv -> {
            statuses.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
    }

    @Test
    void laSaisonSuivanteReprendLesReglagesEtLesStatutsDesClubs() {
        CompetitionDto created = nextSeasonService.createNextSeason(
                1L, new NextSeasonRequestDto(List.of(10L, 11L, 12L), List.of(12L), true));

        assertThat(created.name()).isEqualTo("Finlande - Championnat 2028");
        assertThat(created.season()).isEqualTo(2028);
        assertThat(created.ldcSlots()).isEqualTo(1);
        assertThat(created.relegationSlots()).isEqualTo(1);
        assertThat(created.totalRounds()).isEqualTo(27);
        assertThat(statuses)
                .extracting(
                        s -> s.getTeam().getName(),
                        TeamCompetitionStatus::isDefendingChampion,
                        TeamCompetitionStatus::isPreviousCupWinner,
                        TeamCompetitionStatus::isPromoted)
                .containsExactly(
                        tuple("KUPS", true, false, false),
                        tuple("HJK", false, true, false),
                        tuple("TPS", false, false, true));
        assertThat(saved)
                .extracting(Competition::getName, Competition::getType)
                .containsExactly(
                        tuple("Finlande - Championnat 2028", CompetitionType.LEAGUE),
                        tuple("Finlande - Coupe 2028", CompetitionType.DOMESTIC_CUP));
    }

    @Test
    void unChampionnatNonClotureOuDejaPrepareEstRefuse() {
        NextSeasonRequestDto dto = new NextSeasonRequestDto(List.of(10L), List.of(), false);
        league.setClosedAt(null);
        assertThatThrownBy(() -> nextSeasonService.createNextSeason(1L, dto)).hasMessageContaining("cloture");

        league.setClosedAt(LocalDateTime.now());
        when(competitionRepository.findFirstByTypeAndCountryAndSeason(CompetitionType.LEAGUE, "Finlande", 2028))
                .thenReturn(Optional.of(new Competition()));
        assertThatThrownBy(() -> nextSeasonService.createNextSeason(1L, dto)).hasMessageContaining("existe deja");
        verify(competitionRepository, never()).save(any());
    }

    private Team team(Long id, String name) {
        Team t = new Team(name, "Finlande");
        t.setId(id);
        lenient().when(teamRepository.findById(id)).thenReturn(Optional.of(t));
        return t;
    }
}
