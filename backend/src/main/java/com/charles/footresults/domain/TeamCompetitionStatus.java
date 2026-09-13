package com.charles.footresults.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Statuts manuels d'une equipe dans une competition (LEAGUE) donnee : tenant
 * du titre, promue, vainqueur de la coupe nationale precedente. Ces faits ne
 * sont pas deductibles des donnees importees (une seule saison dans le
 * fichier source), donc saisis a la main plutot que calcules.
 */
@Entity
@Table(name = "team_competition_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"competition_id", "team_id"}))
@Getter
@Setter
@NoArgsConstructor
public class TeamCompetitionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "defending_champion", nullable = false)
    private boolean defendingChampion = false;

    @Column(name = "promoted", nullable = false)
    private boolean promoted = false;

    @Column(name = "previous_cup_winner", nullable = false)
    private boolean previousCupWinner = false;

    /** Competition europeenne jouee la saison precedente ("EL" ou "ECL") ; null si aucune. */
    @Column(name = "previous_europe_competition", length = 10)
    private String previousEuropeCompetition;

    /** Groupe de 2eme phase (ex: "Championnat", "Relegation") ; null si la competition n'est pas scindee. */
    @Column(name = "group_name", length = 60)
    private String groupName;

    public TeamCompetitionStatus(Competition competition, Team team) {
        this.competition = competition;
        this.team = team;
    }
}
