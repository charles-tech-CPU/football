package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Honour;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.PalmaresDto;
import com.charles.footresults.dto.PalmaresDto.Club;
import com.charles.footresults.dto.PalmaresDto.CompetitionHonours;
import com.charles.footresults.dto.PalmaresDto.SeasonHonour;
import com.charles.footresults.dto.PalmaresDto.TitleCount;
import com.charles.footresults.repository.HonourRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PalmaresServiceTest {

    @Mock
    private HonourRepository honourRepository;

    @InjectMocks
    private PalmaresService palmaresService;

    @Test
    void europeEnTeteDesPaysPuisOrdreAlphabetique() {
        when(honourRepository.findScopes()).thenReturn(List.of("Allemagne", "Europe", "France"));

        assertThat(palmaresService.scopes()).containsExactly("Europe", "Allemagne", "France");
    }

    @Test
    void championnatAvantCoupeEtDecompteDesTitres() {
        when(honourRepository.findByScopeOrderBySeasonEndDescIdAsc("France"))
                .thenReturn(List.of(
                        honour("CUP", "Coupe nationale", "2024-2025", "PSG", "Reims", null),
                        honour("LEAGUE", "Championnat", "2024-2025", "PSG", "Marseille", "Monaco"),
                        honour("LEAGUE", "Championnat", "2023-2024", "PSG", "Monaco", "Brest"),
                        honour("LEAGUE", "Championnat", "2022-2023", "Lens", "PSG", "Marseille"),
                        honour("LEAGUE", "Championnat", "2021-2022", "Lille", "PSG", "Monaco")));

        PalmaresDto palmares = palmaresService.palmares("France");

        assertThat(palmares.competitions()).extracting(CompetitionHonours::kind).containsExactly("LEAGUE", "CUP");
        CompetitionHonours league = palmares.competitions().get(0);
        assertThat(league.label()).isEqualTo("Championnat");
        assertThat(league.seasons())
                .extracting(SeasonHonour::season)
                .containsExactly("2024-2025", "2023-2024", "2022-2023", "2021-2022");
        assertThat(league.seasons().get(0).third().name()).isEqualTo("Monaco");
        // A egalite (1 titre), le titre le plus recent d'abord.
        assertThat(league.titles())
                .extracting(t -> t.team().name(), TitleCount::titles, TitleCount::lastSeason)
                .containsExactly(
                        tuple("PSG", 2, "2024-2025"), tuple("Lens", 1, "2022-2023"), tuple("Lille", 1, "2021-2022"));
    }

    @Test
    void clubDeLAppliEtPaysDuClubPourLesCoupesDEurope() {
        Team psg = new Team("PSG", "France");
        psg.setId(7L);
        psg.setLogoPath("psg.png");
        Honour finale = honour("LDC", "Ligue des Champions", "2024-2025", "Paris Saint-Germain", "Inter Milan", null);
        finale.setWinnerTeam(psg);
        finale.setWinnerCountry("France");
        finale.setRunnerUpCountry("Italie");
        when(honourRepository.findByScopeOrderBySeasonEndDescIdAsc("Europe")).thenReturn(List.of(finale));

        SeasonHonour season = palmaresService
                .palmares("Europe")
                .competitions()
                .get(0)
                .seasons()
                .get(0);

        assertThat(season.winner()).isEqualTo(new Club("Paris Saint-Germain", 7L, "psg.png", "France"));
        assertThat(season.runnerUp()).isEqualTo(new Club("Inter Milan", null, null, "Italie"));
        assertThat(season.third()).isNull();
    }

    private static Honour honour(
            String kind, String label, String season, String winner, String runnerUp, String third) {
        Honour h = new Honour();
        h.setScope("France");
        h.setKind(kind);
        h.setCompetitionLabel(label);
        h.setSeason(season);
        h.setSeasonEnd(Integer.parseInt(season.substring(season.length() - 4)));
        h.setWinner(winner);
        h.setRunnerUp(runnerUp);
        h.setThird(third);
        return h;
    }
}
