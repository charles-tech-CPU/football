package com.charles.footresults.web;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.TeamCreateDto;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
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

    public TeamController(
            TeamRepository teamRepository,
            CompetitionRepository competitionRepository,
            TeamCompetitionStatusRepository teamCompetitionStatusRepository,
            TeamService teamService) {
        this.teamRepository = teamRepository;
        this.competitionRepository = competitionRepository;
        this.teamCompetitionStatusRepository = teamCompetitionStatusRepository;
        this.teamService = teamService;
    }

    /**
     * competitionId filtre la liste sur les equipes de cette competition : pour une ligue/coupe
     * nationale (competition.country non nul), toutes les equipes de ce pays ; pour une
     * competition continentale (LDC/EL/EC, country nul), le roster officiel de la phase de
     * ligue (team_competition_status, extrait de la feuille Excel - voir
     * import/extract_group_roster.py) quand il est connu, sinon les equipes ayant deja un match
     * dans cette competition (tours de qualification) en repli.
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
        return source.stream()
                .filter(t -> effectiveCountry == null || effectiveCountry.equalsIgnoreCase(t.getCountry()))
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto create(@Valid @RequestBody TeamCreateDto dto) {
        Team team = new Team(dto.name(), dto.country());
        return TeamDto.from(teamRepository.save(team));
    }

    @PutMapping("/{id}")
    public TeamDto update(@PathVariable Long id, @Valid @RequestBody TeamCreateDto dto) {
        Team team = teamRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + id));
        team.setName(dto.name());
        team.setCountry(dto.country());
        return TeamDto.from(teamRepository.save(team));
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
