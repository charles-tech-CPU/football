package com.charles.footresults.web;

import com.charles.footresults.dto.EuropeanSlotDto;
import com.charles.footresults.dto.EuropeanSlotUpdateDto;
import com.charles.footresults.service.EuropeanSlotService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/european-slots")
public class EuropeanSlotController {

    private final EuropeanSlotService slotService;

    public EuropeanSlotController(EuropeanSlotService slotService) {
        this.slotService = slotService;
    }

    /** GET /api/european-slots?season=2028 (sans saison : la derniere generee) */
    @GetMapping
    public List<EuropeanSlotDto> findBySeason(@RequestParam(required = false) @Nullable Integer season) {
        return slotService.findBySeason(season);
    }

    /** POST /api/european-slots/generate?season=2028 : places reprises de la saison precedente. */
    @PostMapping("/generate")
    public List<EuropeanSlotDto> generate(@RequestParam Integer season) {
        return slotService.generate(season);
    }

    /** POST /api/european-slots/fill?season=2028 : remplissage d'apres les competitions cloturees. */
    @PostMapping("/fill")
    public List<EuropeanSlotDto> fill(@RequestParam Integer season) {
        return slotService.fillAll(season);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EuropeanSlotDto create(@Valid @RequestBody EuropeanSlotUpdateDto dto) {
        return slotService.create(dto);
    }

    @PutMapping("/{id}")
    public EuropeanSlotDto update(@PathVariable Long id, @Valid @RequestBody EuropeanSlotUpdateDto dto) {
        return slotService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        slotService.delete(id);
    }
}
