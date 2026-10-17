package com.charles.footresults.web;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamCreateDto;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.dto.TeamHeadToHeadDto;
import com.charles.footresults.dto.TeamProfileDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import com.charles.footresults.service.TeamHeadToHeadService;
import com.charles.footresults.service.TeamProfileService;
import com.charles.footresults.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamCompetitionStatusRepository teamCompetitionStatusRepository;
    private final TeamService teamService;
    private final TeamProfileService teamProfileService;
    private final TeamHeadToHeadService teamHeadToHeadService;

    public TeamController(
            TeamRepository teamRepository,
            CompetitionRepository competitionRepository,
            TeamCompetitionStatusRepository teamCompetitionStatusRepository,
            TeamService teamService,
            TeamProfileService teamProfileService,
            TeamHeadToHeadService teamHeadToHeadService) {
        this.teamRepository = teamRepository;
        this.competitionRepository = competitionRepository;
        this.teamCompetitionStatusRepository = teamCompetitionStatusRepository;
        this.teamService = teamService;
        this.teamProfileService = teamProfileService;
        this.teamHeadToHeadService = teamHeadToHeadService;
    }

    /**
     * competitionId filtre la liste sur les equipes de cette competition : pour une ligue/coupe
     * nationale (competition.country non nul), toutes les equipes de ce pays ; pour une
     * competition continentale (LDC/EL/EC, country nul), le roster officiel de la phase de
     * ligue (team_competition_status, extrait de la feuille Excel - voir
     * import/extract_group_roster.py) quand il est connu, sinon les equipes ayant deja un match
     * dans cette competition (tours de qualification) en repli. La selection nationale du pays
     * n'est pas proposee dans les listes de clubs d'une competition nationale.
     */
    @GetMapping
    public List<TeamDto> findAll(
            @RequestParam(required = false) String country, @RequestParam(required = false) Long competitionId) {
        List<Team> source = teamRepository.findAll();
        String countryFilter = country;

        if (competitionId != null) {
            Competition competition = competitionRepository
                    .findById(competitionId)
                    .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + competitionId));
            if (competition.getCountry() != null) {
                countryFilter = competition.getCountry();
            } else {
                List<TeamCompetitionStatus> roster = teamCompetitionStatusRepository.findByCompetitionId(competitionId);
                source = roster.isEmpty()
                        ? teamRepository.findByCompetitionId(competitionId)
                        : roster.stream().map(TeamCompetitionStatus::getTeam).toList();
            }
        }

        final String effectiveCountry = countryFilter;
        final boolean clubsOnly = competitionId != null && countryFilter != null;
        return source.stream()
                .filter(t -> effectiveCountry == null || effectiveCountry.equalsIgnoreCase(t.getCountry()))
                .filter(t -> !clubsOnly || !t.isNationalTeam())
                .sorted(Comparator.comparing(Team::getName))
                .map(TeamDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TeamDto findOne(@PathVariable Long id) {
        return teamRepository
                .findById(id)
                .map(TeamDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
    }

    /** GET /api/teams/{id}/profile : fiche club (classements, forme, derniers et prochains matchs). */
    @GetMapping("/{id}/profile")
    public TeamProfileDto profile(@PathVariable Long id) {
        return teamProfileService.profile(id);
    }

    /** GET /api/teams/{id}/head-to-head?opponentId=42 : bilan de toutes les confrontations entre les deux equipes. */
    @GetMapping("/{id}/head-to-head")
    public TeamHeadToHeadDto headToHead(@PathVariable Long id, @RequestParam Long opponentId) {
        return teamHeadToHeadService.headToHead(id, opponentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto create(@Valid @RequestBody TeamCreateDto dto) {
        String name = normalizeName(dto.name());
        rejectDuplicateName(name, null);
        Team team = new Team(name, dto.country());
        return TeamDto.from(teamRepository.save(team));
    }

    @PutMapping("/{id}")
    public TeamDto update(@PathVariable Long id, @Valid @RequestBody TeamCreateDto dto) {
        Team team = teamRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
        String name = normalizeName(dto.name());
        rejectDuplicateName(name, id);
        team.setName(name);
        team.setCountry(dto.country());
        return TeamDto.from(teamRepository.save(team));
    }

    /** "  fc   noah " -> "fc noah" : evite les doublons qui ne different que par les espaces. */
    private static String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    /** Refuse un nom deja porte par un autre club (insensible a la casse). */
    private void rejectDuplicateName(String name, Long currentId) {
        teamRepository
                .findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Un club nomme " + existing.getName() + " existe deja (" + existing.getCountry() + ")");
                });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamRepository.deleteById(id);
    }

    /** POST /api/teams/{id}/merge?intoId=42 : reaffecte les matchs de {id} vers intoId, puis supprime {id}. */
    @PostMapping("/{id}/merge")
    public TeamDto merge(@PathVariable Long id, @RequestParam Long intoId) {
        return teamService.merge(id, intoId);
    }
}
