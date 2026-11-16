package com.charles.footresults.web;

import com.charles.footresults.dto.RivalryCreateDto;
import com.charles.footresults.dto.RivalryDto;
import com.charles.footresults.service.RivalryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Rivalites entre clubs (derbys...), signalees dans le calendrier. */
@RestController
@RequestMapping("/api/rivalries")
public class RivalryController {

    private final RivalryService rivalryService;

    public RivalryController(RivalryService rivalryService) {
        this.rivalryService = rivalryService;
    }

    @GetMapping
    public List<RivalryDto> findAll() {
        return rivalryService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RivalryDto create(@Valid @RequestBody RivalryCreateDto dto) {
        return rivalryService.create(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        rivalryService.delete(id);
    }
}
