package com.charles.footresults.web;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.dto.CompetitionCreateDto;
import com.charles.footresults.dto.CompetitionDto;
import com.charles.footresults.dto.QualificationSlotsDto;
import com.charles.footresults.repository.CompetitionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionRepository competitionRepository;

    public CompetitionController(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @GetMapping
    public List<CompetitionDto> findAll() {
        return competitionRepository.findAll().stream()
                .sorted(Comparator.comparing(Competition::getType)
                        .thenComparing(Competition::getCode))
                .map(CompetitionDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CompetitionDto findOne(@PathVariable Long id) {
        return competitionRepository.findById(id).map(CompetitionDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionDto create(@Valid @RequestBody CompetitionCreateDto dto) {
        Competition competition = new Competition(dto.code(), dto.name(), dto.type(), dto.country(), dto.season());
        return CompetitionDto.from(competitionRepository.save(competition));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        competitionRepository.deleteById(id);
    }

    /** PATCH /api/competitions/{id}/qualification-slots */
    @PatchMapping("/{id}/qualification-slots")
    public CompetitionDto updateQualificationSlots(@PathVariable Long id, @Valid @RequestBody QualificationSlotsDto dto) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + id));
        competition.setLdcSlots(dto.ldcSlots());
        competition.setElSlots(dto.elSlots());
        competition.setEclSlots(dto.eclSlots());
        return CompetitionDto.from(competitionRepository.save(competition));
    }
}
