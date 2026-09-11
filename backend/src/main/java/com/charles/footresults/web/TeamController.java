package com.charles.footresults.web;

import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.TeamCreateDto;
import com.charles.footresults.dto.TeamDto;
import com.charles.footresults.repository.TeamRepository;
import com.charles.footresults.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final TeamService teamService;

    public TeamController(TeamRepository teamRepository, TeamService teamService) {
        this.teamRepository = teamRepository;
        this.teamService = teamService;
    }

    @GetMapping
    public List<TeamDto> findAll(@RequestParam(required = false) String country) {
        return teamRepository.findAll().stream()
                .filter(t -> country == null || country.equalsIgnoreCase(t.getCountry()))
                .sorted(Comparator.comparing(Team::getName))
                .map(TeamDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TeamDto findOne(@PathVariable Long id) {
        return teamRepository.findById(id).map(TeamDto::from)
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
        Team team = teamRepository.findById(id)
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
