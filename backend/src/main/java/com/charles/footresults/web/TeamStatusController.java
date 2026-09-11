package com.charles.footresults.web;

import com.charles.footresults.dto.TeamStatusDto;
import com.charles.footresults.dto.TeamStatusUpdateDto;
import com.charles.footresults.service.TeamStatusService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-status")
public class TeamStatusController {

    private final TeamStatusService teamStatusService;

    public TeamStatusController(TeamStatusService teamStatusService) {
        this.teamStatusService = teamStatusService;
    }

    /** GET /api/team-status?competitionId=1 */
    @GetMapping
    public List<TeamStatusDto> findByCompetition(@RequestParam Long competitionId) {
        return teamStatusService.findByCompetition(competitionId);
    }

    /** PUT /api/team-status?competitionId=1&teamId=3 */
    @PutMapping
    public TeamStatusDto upsert(@RequestParam Long competitionId, @RequestParam Long teamId,
                                 @Valid @RequestBody TeamStatusUpdateDto dto) {
        return teamStatusService.upsert(competitionId, teamId, dto);
    }
}
