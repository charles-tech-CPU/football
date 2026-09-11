package com.charles.footresults.web;

import com.charles.footresults.dto.HeadToHeadCellDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.service.StandingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    /** GET /api/standings?competitionId=1 */
    @GetMapping("/api/standings")
    public List<StandingRowDto> standings(@RequestParam Long competitionId) {
        return standingsService.computeStandings(competitionId);
    }

    /** GET /api/head-to-head?competitionId=1 */
    @GetMapping("/api/head-to-head")
    public List<HeadToHeadCellDto> headToHead(@RequestParam Long competitionId) {
        return standingsService.computeHeadToHead(competitionId);
    }
}
