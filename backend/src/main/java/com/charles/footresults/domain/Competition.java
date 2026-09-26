package com.charles.footresults.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "competition")
@Getter
@Setter
@NoArgsConstructor
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code court, ex: "FRANCE", "LDC", "EL", "EC". */
    @Column(nullable = false, length = 40)
    private String code;

    /** Nom affichable, ex: "France - Championnat 2027". */
    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompetitionType type;

    /** Pays pour une ligue/coupe nationale ; null pour une competition continentale. */
    @Column(length = 60)
    private String country;

    @Column(nullable = false)
    private Integer season;

    /** Nombre de places qualificatives par rang de classement (LEAGUE uniquement). Saisi a la main, pays par pays. */
    @Column(name = "ldc_slots", nullable = false)
    private Integer ldcSlots = 0;

    @Column(name = "el_slots", nullable = false)
    private Integer elSlots = 0;

    @Column(name = "ecl_slots", nullable = false)
    private Integer eclSlots = 0;

    /** Nombre de places de relegation directe (dernieres places du classement, LEAGUE uniquement). */
    @Column(name = "relegation_slots", nullable = false)
    private Integer relegationSlots = 0;

    /** Nombre de places de barrage de maintien, juste au-dessus de la zone de relegation directe. */
    @Column(name = "barrage_slots", nullable = false)
    private Integer barrageSlots = 0;

    /** Nombre de journees attendues (LEAGUE uniquement), pour suivre l'avancement du calendrier. Null si inconnu. */
    @Column(name = "total_rounds")
    private Integer totalRounds;

    public Competition(String code, String name, CompetitionType type, String country, Integer season) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.country = country;
        this.season = season;
    }
}
