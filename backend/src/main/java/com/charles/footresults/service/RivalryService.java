package com.charles.footresults.service;

import com.charles.footresults.domain.Rivalry;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.RivalryCreateDto;
import com.charles.footresults.dto.RivalryDto;
import com.charles.footresults.repository.RivalryRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rivalites entre clubs (derbys et grandes rivalites) : liste de depart (V139) completee depuis
 * l'appli. Un match entre deux rivaux porte une icone dans le calendrier (MatchHighlightService).
 */
@Service
@Transactional
public class RivalryService {

    /** Libelle affiche pour une rivalite sans nom. */
    static final String DEFAULT_LABEL = "Rivalité";

    private final RivalryRepository rivalryRepository;
    private final TeamRepository teamRepository;

    public RivalryService(RivalryRepository rivalryRepository, TeamRepository teamRepository) {
        this.rivalryRepository = rivalryRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<RivalryDto> findAll() {
        return rivalryRepository.findAll().stream()
                .map(RivalryDto::from)
                .sorted(Comparator.comparing(RivalryDto::country, Comparator.nullsLast(String::compareTo))
                        .thenComparing(RivalryDto::team1Name)
                        .thenComparing(RivalryDto::team2Name))
                .toList();
    }

    public RivalryDto create(RivalryCreateDto dto) {
        if (dto.team1Id().equals(dto.team2Id())) {
            throw new IllegalArgumentException("Une rivalite oppose deux clubs differents");
        }
        Team a = team(dto.team1Id());
        Team b = team(dto.team2Id());
        Team first = a.getId() < b.getId() ? a : b;
        Team second = first == a ? b : a;
        if (rivalryRepository
                .findByTeam1_IdAndTeam2_Id(first.getId(), second.getId())
                .isPresent()) {
            throw new IllegalArgumentException(
                    "La rivalite " + first.getName() + " - " + second.getName() + " existe deja");
        }
        Rivalry rivalry = new Rivalry();
        rivalry.setTeam1(first);
        rivalry.setTeam2(second);
        String label = dto.label() == null ? "" : dto.label().strip();
        rivalry.setLabel(label.isEmpty() ? null : label);
        return RivalryDto.from(rivalryRepository.save(rivalry));
    }

    public void delete(Long id) {
        rivalryRepository.deleteById(id);
    }

    /** Libelle de rivalite par paire de clubs (cle : pairKey), "Rivalité" pour une rivalite sans nom. */
    @Transactional(readOnly = true)
    public Map<String, String> labelsByPair() {
        Map<String, String> result = new HashMap<>();
        for (Rivalry r : rivalryRepository.findAll()) {
            result.put(
                    pairKey(r.getTeam1().getId(), r.getTeam2().getId()),
                    r.getLabel() == null ? DEFAULT_LABEL : r.getLabel());
        }
        return result;
    }

    /** Cle d'une paire de clubs, independante de l'ordre (domicile / exterieur). */
    public static String pairKey(Long teamA, Long teamB) {
        return Math.min(teamA, teamB) + "-" + Math.max(teamA, teamB);
    }

    private Team team(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
    }
}
