package com.charles.footresults.web;

import com.charles.footresults.dto.PalmaresDto;
import com.charles.footresults.service.PalmaresService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/palmares")
public class PalmaresController {

    private final PalmaresService palmaresService;

    public PalmaresController(PalmaresService palmaresService) {
        this.palmaresService = palmaresService;
    }

    /** GET /api/palmares/scopes : pays (et "Europe") ayant un palmares. */
    @GetMapping("/scopes")
    public List<String> scopes() {
        return palmaresService.scopes();
    }

    /** GET /api/palmares?scope=France */
    @GetMapping
    public PalmaresDto palmares(@RequestParam String scope) {
        return palmaresService.palmares(scope);
    }
}
