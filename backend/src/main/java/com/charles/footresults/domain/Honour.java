package com.charles.footresults.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Une saison du palmares historique d'une competition (onglet Palmares) : champion / vainqueur de
 * la coupe, 2e / finaliste et 3e quand la source les donne. Importe de Wikipedia par
 * import/palmares.py ; noms de clubs gardes tels quels (beaucoup n'existent plus), relies au Team
 * de l'appli quand il existe (logo). Une saison peut avoir plusieurs lignes (Coupe Intertoto).
 */
@Entity
@Table(name = "honour")
@Getter
@Setter
@NoArgsConstructor
public class Honour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pays (meme libelle que Competition.country) ou "Europe" pour les coupes d'Europe. */
    @Column(nullable = false, length = 60)
    private String scope;

    /** LEAGUE / CUP pour un pays ; LDC / EL / ECL / C2 pour les coupes d'Europe. */
    @Column(nullable = false, length = 20)
    private String kind;

    @Column(name = "competition_label", nullable = false, length = 80)
    private String competitionLabel;

    /** "2023-2024", ou "2024" pour un championnat sur l'annee civile. */
    @Column(nullable = false, length = 20)
    private String season;

    /** Annee de fin de saison, pour le tri. */
    @Column(name = "season_end", nullable = false)
    private Integer seasonEnd;

    @Column(nullable = false, length = 150)
    private String winner;

    @Column(name = "runner_up", length = 150)
    private String runnerUp;

    @Column(length = 150)
    private String third;

    /** Pays du vainqueur / du finaliste (coupes d'Europe uniquement ; libelle francais, cf. Team.country). */
    @Column(name = "winner_country", length = 60)
    private String winnerCountry;

    @Column(name = "runner_up_country", length = 60)
    private String runnerUpCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_up_team_id")
    private Team runnerUpTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "third_team_id")
    private Team thirdTeam;
}
