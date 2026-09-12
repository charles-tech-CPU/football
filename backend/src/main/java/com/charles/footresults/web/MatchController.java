package com.charles.footresults.web;

import com.charles.footresults.dto.MatchCreateDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.MatchPageDto;
import com.charles.footresults.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /** GET /api/matches?competitionId=1  ou  GET /api/matches?teamId=3 */
    @GetMapping
    public List<MatchDto> find(@RequestParam(required = false) Long competitionId,
                                @RequestParam(required = false) Long teamId) {
        if (competitionId != null) {
            return matchService.findByCompetition(competitionId);
        }
        if (teamId != null) {
            return matchService.findByTeam(teamId);
        }
        throw new IllegalArgumentException("Precise competitionId ou teamId en parametre de requete");
    }

    /** GET /api/matches/recent?limit=300 : derniers matchs joues, toutes competitions confondues. */
    @GetMapping("/recent")
    public List<MatchDto> recent(@RequestParam(defaultValue = "300") int limit) {
        return matchService.findRecentResults(limit);
    }

    /**
     * GET /api/matches/upcoming?page=0&size=20&filter=league : matchs pas encore joues, paginés,
     * filtre optionnel parmi "league"/"cup"/"ldc"/"el"/"ec" (defaut : tout).
     */
    @GetMapping("/upcoming")
    public MatchPageDto upcoming(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) String filter) {
        return matchService.findUpcoming(page, size, filter);
    }

    /** GET /api/matches/postponed : matchs reportes/suspendus, toutes competitions confondues, sans limite. */
    @GetMapping("/postponed")
    public List<MatchDto> postponed() {
        return matchService.findPostponedOrSuspended();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchDto create(@Valid @RequestBody MatchCreateDto dto) {
        return matchService.create(dto);
    }

    @PutMapping("/{id}")
    public MatchDto update(@PathVariable Long id, @Valid @RequestBody MatchCreateDto dto) {
        return matchService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        matchService.delete(id);
    }
}
