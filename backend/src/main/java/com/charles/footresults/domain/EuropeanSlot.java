package com.charles.footresults.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Une place d'un pays en coupe d'Europe pour une saison (cf. EuropeanSlotService) : competition,
 * tour d'entree, voie d'acces (champion, 2e du championnat, vainqueur de coupe...) et club qualifie
 * une fois connu.
 */
@Entity
@Table(name = "european_slot")
@Getter
@Setter
@NoArgsConstructor
public class EuropeanSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Saison UEFA (meme convention que Competition.season : 2028 = 2027-2028). */
    @Column(nullable = false)
    private Integer season;

    /** "LDC", "EL" ou "EC" (codes des competitions continentales). */
    @Column(name = "competition_code", nullable = false, length = 10)
    private String competitionCode;

    /** Tour d'entree, du plus avance (1 = phase de ligue) au premier tour de qualification (cf. EuropeanSlotService.STAGES). */
    @Column(name = "entry_stage", nullable = false)
    private Integer entryStage;

    @Column(nullable = false, length = 60)
    private String country;

    /** Voie d'acces ("Champion", "3e du championnat", "Vainqueur de coupe"...) ; null tant qu'elle n'est pas connue. */
    @Column(length = 60)
    private String route;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    /** Club choisi a la main : jamais remplace par le remplissage automatique. */
    @Column(nullable = false)
    private boolean manual;
}
