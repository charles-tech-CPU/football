package com.charles.footresults.web;

import com.charles.footresults.dto.ClosureDto;
import com.charles.footresults.dto.ClosureRequestDto;
import com.charles.footresults.dto.CompetitionDto;
import com.charles.footresults.dto.NextSeasonRequestDto;
import com.charles.footresults.service.CompetitionClosureService;
import com.charles.footresults.service.NextSeasonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/competitions/{id}")
/** Fin de saison d'une competition : cloture, reouverture et preparation de la saison suivante. */
public class ClosureController {

    private final CompetitionClosureService closureService;
    private final NextSeasonService nextSeasonService;

    public ClosureController(CompetitionClosureService closureService, NextSeasonService nextSeasonService) {
        this.closureService = closureService;
        this.nextSeasonService = nextSeasonService;
    }

    /** GET /api/competitions/{id}/closure : etat de cloture, saison proposee, classement fige. */
    @GetMapping("/closure")
    public ClosureDto closure(@PathVariable Long id) {
        return closureService.closure(id);
    }

    /** POST /api/competitions/{id}/closure : cloture (ou recloture) la competition. */
    @PostMapping("/closure")
    public ClosureDto close(@PathVariable Long id, @Valid @RequestBody ClosureRequestDto dto) {
        return closureService.close(id, dto);
    }

    /** DELETE /api/competitions/{id}/closure : rouvre la competition. */
    @DeleteMapping("/closure")
    public ClosureDto reopen(@PathVariable Long id) {
        return closureService.reopen(id);
    }

    /** POST /api/competitions/{id}/next-season : prepare la saison suivante d'un championnat cloture. */
    @PostMapping("/next-season")
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionDto nextSeason(@PathVariable Long id, @Valid @RequestBody NextSeasonRequestDto dto) {
        return nextSeasonService.createNextSeason(id, dto);
    }
}
