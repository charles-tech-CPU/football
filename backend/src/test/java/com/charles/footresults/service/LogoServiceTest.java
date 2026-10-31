package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.TeamRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class LogoServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @TempDir
    private Path logosDir;

    private LogoService logoService;
    private Team tps;

    @BeforeEach
    void setUp() {
        logoService = new LogoService(teamRepository, logosDir.resolve("logos").toString());
        tps = new Team("TPS", "Finlande");
        tps.setId(12L);
        when(teamRepository.findById(12L)).thenReturn(Optional.of(tps));
    }

    @Test
    void leLogoEstEnregistreDansLeDossierEtRattacheAuClub() throws Exception {
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};

        TeamDto updated = logoService.upload(12L, new MockMultipartFile("file", "tps.png", "image/png", png));

        assertThat(updated.logoPath()).startsWith("12-").endsWith(".png");
        assertThat(Files.readAllBytes(logosDir.resolve("logos").resolve(updated.logoPath())))
                .isEqualTo(png);
    }

    @Test
    void unFichierQuiNEstPasUneImageEstRefuse() {
        MockMultipartFile pdf = new MockMultipartFile("file", "tps.pdf", "application/pdf", new byte[] {1});

        assertThatThrownBy(() -> logoService.upload(12L, pdf)).hasMessageContaining("PNG");
        assertThat(tps.getLogoPath()).isNull();
    }
}
