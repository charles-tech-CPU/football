package com.charles.footresults.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.dto.CompetitionCreateDto;
import com.charles.footresults.dto.CompetitionDto;
import com.charles.footresults.dto.QualificationSlotsDto;
import com.charles.footresults.repository.CompetitionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompetitionControllerTest {

    private static final QualificationSlotsDto SLOTS = new QualificationSlotsDto(4, 2, 1, 2, 1);

    @Mock
    private CompetitionRepository competitionRepository;

    @InjectMocks
    private CompetitionController competitionController;

    @Test
    void toutesLesCompetitionsTrieesParTypePuisParCode() {
        when(competitionRepository.findAll())
                .thenReturn(List.of(
                        competition(1L, "LDC", CompetitionType.CONTINENTAL_CUP),
                        competition(2L, "FRANCE", CompetitionType.LEAGUE),
                        competition(3L, "ALBANIE", CompetitionType.LEAGUE)));

        assertThat(competitionController.findAll(null))
                .extracting(CompetitionDto::code)
                .containsExactly("ALBANIE", "FRANCE", "LDC");
    }

    @Test
    void filtreParType() {
        when(competitionRepository.findByType(CompetitionType.INTERNATIONAL))
                .thenReturn(List.of(competition(4L, "CAF_CAN_2027", CompetitionType.INTERNATIONAL)));

        assertThat(competitionController.findAll(CompetitionType.INTERNATIONAL))
                .extracting(CompetitionDto::code)
                .containsExactly("CAF_CAN_2027");
    }

    @Test
    void findOneRenvoieToutesLesInformations() {
        Competition france = competition(2L, "FRANCE", CompetitionType.LEAGUE);
        france.setLdcSlots(4);
        france.setRelegationSlots(2);
        when(competitionRepository.findById(2L)).thenReturn(Optional.of(france));

        assertThat(competitionController.findOne(2L))
                .isEqualTo(new CompetitionDto(
                        2L, "FRANCE", "Competition FRANCE", CompetitionType.LEAGUE, "France", 2027, 4, 0, 0, 2, 0));
    }

    @Test
    void uneCompetitionInconnueLeveUneErreurEnLectureCommeEnModification() {
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competitionController.findOne(99L)).isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> competitionController.updateQualificationSlots(99L, SLOTS))
                .isInstanceOf(EntityNotFoundException.class);
        verify(competitionRepository, never()).save(any());
    }

    @Test
    void creation() {
        when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> {
            Competition saved = inv.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        CompetitionDto created = competitionController.create(new CompetitionCreateDto(
                "SUISSE", "Suisse - Championnat 2027", CompetitionType.LEAGUE, "Suisse", 2027));

        assertThat(created.id()).isEqualTo(7L);
        assertThat(created.code()).isEqualTo("SUISSE");
        assertThat(created.ldcSlots()).isZero();
    }

    @Test
    void miseAJourDesPlacesQualificatives() {
        Competition france = competition(2L, "FRANCE", CompetitionType.LEAGUE);
        when(competitionRepository.findById(2L)).thenReturn(Optional.of(france));
        when(competitionRepository.save(france)).thenReturn(france);

        CompetitionDto updated = competitionController.updateQualificationSlots(2L, SLOTS);

        assertThat(List.of(
                        updated.ldcSlots(),
                        updated.elSlots(),
                        updated.eclSlots(),
                        updated.relegationSlots(),
                        updated.barrageSlots()))
                .containsExactly(4, 2, 1, 2, 1);
    }

    @Test
    void suppression() {
        competitionController.delete(3L);

        verify(competitionRepository).deleteById(3L);
    }

    private static Competition competition(Long id, String code, CompetitionType type) {
        Competition competition = new Competition(code, "Competition " + code, type, "France", 2027);
        competition.setId(id);
        return competition;
    }
}
