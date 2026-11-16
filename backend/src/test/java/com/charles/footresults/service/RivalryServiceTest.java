package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Rivalry;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.RivalryCreateDto;
import com.charles.footresults.dto.RivalryDto;
import com.charles.footresults.repository.RivalryRepository;
import com.charles.footresults.repository.TeamRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RivalryServiceTest {

    @Mock
    private RivalryRepository rivalryRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private RivalryService service;

    @Test
    void uneRivaliteEstEnregistreeDansLOrdreDesIds() {
        Team psg = team(20L, "PSG");
        Team om = team(10L, "MARSEILLE");
        when(teamRepository.findById(20L)).thenReturn(Optional.of(psg));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(om));
        when(rivalryRepository.findByTeam1_IdAndTeam2_Id(10L, 20L)).thenReturn(Optional.empty());
        when(rivalryRepository.save(any(Rivalry.class))).thenAnswer(inv -> inv.getArgument(0));

        RivalryDto dto = service.create(new RivalryCreateDto(20L, 10L, "  "));

        assertThat(dto.team1Name()).isEqualTo("MARSEILLE");
        assertThat(dto.team2Name()).isEqualTo("PSG");
        assertThat(dto.label()).isNull();
    }

    @Test
    void uneRivaliteEnDoubleOuAvecSoiMemeEstRefusee() {
        Team psg = team(20L, "PSG");
        Team om = team(10L, "MARSEILLE");
        when(teamRepository.findById(20L)).thenReturn(Optional.of(psg));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(om));
        when(rivalryRepository.findByTeam1_IdAndTeam2_Id(10L, 20L)).thenReturn(Optional.of(new Rivalry()));
        RivalryCreateDto doublon = new RivalryCreateDto(10L, 20L, "Le Classique");
        RivalryCreateDto soiMeme = new RivalryCreateDto(10L, 10L, null);

        assertThatThrownBy(() -> service.create(doublon)).hasMessageContaining("existe deja");
        assertThatThrownBy(() -> service.create(soiMeme)).hasMessageContaining("deux clubs differents");
        verify(rivalryRepository, never()).save(any());
    }

    @Test
    void laCleDePaireNeDependPasDuTerrain() {
        assertThat(RivalryService.pairKey(20L, 10L)).isEqualTo(RivalryService.pairKey(10L, 20L));
    }

    private static Team team(Long id, String name) {
        Team t = new Team();
        t.setId(id);
        t.setName(name);
        t.setCountry("France");
        return t;
    }
}
