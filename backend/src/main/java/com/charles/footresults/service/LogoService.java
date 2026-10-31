package com.charles.footresults.service;

import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Blason envoye depuis l'appli (club cree en cours de saison, promu...) : enregistre dans le dossier
 * app.logos.dir, hors du jar (les blasons importes y sont embarques et ne se modifient pas), et
 * servi sous /logos/ comme les autres (cf. WebConfig). Le nom du fichier change a chaque envoi pour
 * que le navigateur ne garde pas l'ancien en cache.
 */
@Service
public class LogoService {

    private static final Map<String, String> EXTENSIONS =
            Map.of("image/png", "png", "image/jpeg", "jpg", "image/webp", "webp", "image/gif", "gif");

    private final TeamRepository teamRepository;
    private final Path logosDir;

    public LogoService(TeamRepository teamRepository, @Value("${app.logos.dir}") String logosDir) {
        this.teamRepository = teamRepository;
        this.logosDir = Path.of(logosDir);
    }

    public TeamDto upload(Long teamId, MultipartFile file) {
        Team team = teamRepository
                .findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + teamId));
        String extension = EXTENSIONS.get(file.getContentType());
        if (file.isEmpty() || extension == null) {
            throw new IllegalArgumentException("Logo attendu au format PNG, JPEG, WebP ou GIF");
        }
        String fileName = teamId + "-" + System.currentTimeMillis() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(logosDir);
            Files.copy(in, logosDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'enregistrer le logo", e);
        }
        team.setLogoPath(fileName);
        return TeamDto.from(teamRepository.save(team));
    }
}
