package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.FinalStanding;
import com.charles.footresults.domain.Honour;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.ClosureRequestDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.FinalStandingRepository;
import com.charles.footresults.repository.HonourRepository;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompetitionClosureServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private FinalStandingRepository finalStandingRepository;

    @Mock
    private HonourRepository honourRepository;

    @Mock
    private StandingsService standingsService;

    @Mock
    private EuropeanSlotService europeanSlotService;

    @InjectMocks
    private CompetitionClosureService closureService;

    private Competition finlande;
    private Competition coupe;
    private Team kups;
    private Team hjk;
    private Team ilves;

    @BeforeEach
    void setUp() {
        finlande = competition(1L, CompetitionType.LEAGUE);
        coupe = competition(2L, CompetitionType.DOMESTIC_CUP);
        kups = team(10L, "KUPS");
        hjk = team(11L, "HJK");
        ilves = team(12L, "ILVES");
        for (Team t : List.of(kups, hjk, ilves)) {
            lenient().when(teamRepository.findById(t.getId())).thenReturn(Optional.of(t));
        }
        lenient().when(honourRepository.findByTeam(anyLong())).thenReturn(List.of());
        lenient()
                .when(honourRepository.findFirstByScopeAndKindAndSeason(any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient()
                .when(finalStandingRepository.findByCompetitionIdOrderByFinalRankAsc(anyLong()))
                .thenReturn(List.of());
    }

    @Test
    void cloturerUnChampionnatFigeLOrdreValideEtRenseigneLePalmares() {
        when(standingsService.computeLiveStandings(1L))
                .thenReturn(List.of(row(hjk, 50), row(kups, 50), row(ilves, 40)));
        List<FinalStanding> saved = new ArrayList<>();
        when(finalStandingRepository.save(any(FinalStanding.class))).thenAnswer(inv -> {
            saved.add(inv.getArgument(0));
            return inv.getArgument(0);
        });

        // KuPS devant HJK a egalite de points (tete-a-tete) : ordre choisi a la main.
        closureService.close(1L, new ClosureRequestDto("2026", List.of(10L, 11L, 12L), null, null));

        assertThat(saved)
                .extracting(f -> f.getTeam().getName(), FinalStanding::getFinalRank, FinalStanding::getPoints)
                .containsExactly(tuple("KUPS", 1, 50), tuple("HJK", 2, 50), tuple("ILVES", 3, 40));
        Honour honour = savedHonour();
        assertThat(honour.getScope()).isEqualTo("Finlande");
        assertThat(honour.getKind()).isEqualTo("LEAGUE");
        assertThat(honour.getSeasonEnd()).isEqualTo(2026);
        assertThat(honour.getWinnerTeam()).isEqualTo(kups);
        assertThat(honour.getRunnerUpTeam()).isEqualTo(hjk);
        assertThat(honour.getThirdTeam()).isEqualTo(ilves);
        assertThat(finlande.isClosed()).isTrue();
        assertThat(finlande.getWinnerTeam()).isEqualTo(kups);
        verify(europeanSlotService).fill(2028, "Finlande");
    }

    @Test
    void unClassementIncompletEstRefuse() {
        when(standingsService.computeLiveStandings(1L)).thenReturn(List.of(row(hjk, 50), row(kups, 50)));
        ClosureRequestDto dto = new ClosureRequestDto("2026", List.of(10L, 10L), null, null);

        assertThatThrownBy(() -> closureService.close(1L, dto)).hasMessageContaining("une fois chacune des 2 equipes");
        verify(finalStandingRepository, never()).save(any());
        assertThat(finlande.isClosed()).isFalse();
    }

    @Test
    void lePalmaresReprendLeNomDejaUtiliseEtCompleteLaSaisonExistante() {
        Honour previous = new Honour();
        previous.setWinner("KuPS");
        previous.setWinnerTeam(kups);
        when(honourRepository.findByTeam(10L)).thenReturn(List.of(previous));
        Honour imported = new Honour();
        imported.setScope("Finlande");
        imported.setKind("CUP");
        imported.setSeason("2026");
        imported.setWinner("Inter Turku");
        when(honourRepository.findFirstByScopeAndKindAndSeason("Finlande", "CUP", "2026"))
                .thenReturn(Optional.of(imported));

        closureService.close(2L, new ClosureRequestDto("2026", null, 10L, 11L));

        Honour honour = savedHonour();
        assertThat(honour).isSameAs(imported);
        assertThat(honour.getWinner()).isEqualTo("KuPS");
        assertThat(honour.getRunnerUp()).isEqualTo("HJK");
        assertThat(coupe.isClosed()).isTrue();
    }

    @Test
    void uneCoupeSansVainqueurOuUneSaisonMalFormeeEstRefusee() {
        ClosureRequestDto sansVainqueur = new ClosureRequestDto("2026", null, null, 11L);
        ClosureRequestDto saisonInvalide = new ClosureRequestDto("26/27", null, 10L, 11L);

        assertThatThrownBy(() -> closureService.close(2L, sansVainqueur)).hasMessageContaining("Vainqueur");
        assertThatThrownBy(() -> closureService.close(2L, saisonInvalide)).hasMessageContaining("format");
    }

    @Test
    void cloturerUneCoupeDEuropeAlimenteLePalmaresEuropeAvecLesPays() {
        Competition ldc =
                new Competition("LDC", "Ligue des Champions 2027", CompetitionType.CONTINENTAL_CUP, null, 2027);
        ldc.setId(3L);
        when(competitionRepository.findById(3L)).thenReturn(Optional.of(ldc));
        Team psg = new Team("PSG", "France");
        psg.setId(20L);
        when(teamRepository.findById(20L)).thenReturn(Optional.of(psg));

        closureService.close(3L, new ClosureRequestDto("2026-2027", null, 20L, 10L));

        Honour honour = savedHonour();
        assertThat(honour.getScope()).isEqualTo("Europe");
        assertThat(honour.getKind()).isEqualTo("LDC");
        assertThat(honour.getCompetitionLabel()).isEqualTo("Ligue des Champions");
        assertThat(honour.getSeasonEnd()).isEqualTo(2027);
        assertThat(honour.getWinnerCountry()).isEqualTo("France");
        assertThat(honour.getRunnerUpCountry()).isEqualTo("Finlande");
        assertThat(ldc.isClosed()).isTrue();
        verify(europeanSlotService, never()).fill(any(), any());
    }

    @Test
    void rouvrirSupprimeLeClassementFige() {
        finlande.setClosedAt(LocalDateTime.now());

        closureService.reopen(1L);

        verify(finalStandingRepository).deleteByCompetitionId(1L);
        assertThat(finlande.isClosed()).isFalse();
    }

    @Test
    void laSaisonProposeeSuitLeFormatDuPalmares() {
        when(matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(1L))
                .thenReturn(List.of(match(2026, 8), match(2027, 5)));
        Honour lastSeason = new Honour();
        lastSeason.setSeason("2025-2026");
        when(honourRepository.findFirstByScopeAndKindOrderBySeasonEndDesc("Finlande", "LEAGUE"))
                .thenReturn(Optional.of(lastSeason));
        assertThat(closureService.suggestedSeason(finlande)).isEqualTo("2026-2027");

        lastSeason.setSeason("2025");
        when(matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(1L))
                .thenReturn(List.of(match(2026, 4), match(2026, 11)));
        assertThat(closureService.suggestedSeason(finlande)).isEqualTo("2026");
    }

    private Honour savedHonour() {
        ArgumentCaptor<Honour> captor = ArgumentCaptor.forClass(Honour.class);
        verify(honourRepository).save(captor.capture());
        return captor.getValue();
    }

    private Competition competition(Long id, CompetitionType type) {
        Competition c = new Competition("FINLANDE", "Finlande", type, "Finlande", 2027);
        c.setId(id);
        lenient().when(competitionRepository.findById(id)).thenReturn(Optional.of(c));
        return c;
    }

    private static Team team(Long id, String name) {
        Team t = new Team(name, "Finlande");
        t.setId(id);
        return t;
    }

    private static StandingRowDto row(Team team, int points) {
        return new StandingRowDto(
                team.getId(), team.getName(), null, "Finlande", 22, 0, 0, 0, 30, 20, 10, points, null);
    }

    private static Match match(int year, int month) {
        Match m = new Match();
        m.setDate(LocalDate.of(year, month, 1));
        return m;
    }
}
