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
 * Une ligne du classement final fige a la cloture d'un championnat (cf. CompetitionClosureService) :
 * rang valide a la main et statistiques de l'equipe au moment de la cloture.
 */
@Entity
@Table(name = "final_standing")
@Getter
@Setter
@NoArgsConstructor
public class FinalStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "final_rank", nullable = false)
    private int finalRank;

    /** Mini-championnat de 2e phase de l'equipe ; null si la competition n'est pas scindee. */
    @Column(name = "group_name", length = 60)
    private String groupName;

    @Column(nullable = false)
    private int played;

    @Column(nullable = false)
    private int won;

    @Column(nullable = false)
    private int drawn;

    @Column(nullable = false)
    private int lost;

    @Column(name = "goals_for", nullable = false)
    private int goalsFor;

    @Column(name = "goals_against", nullable = false)
    private int goalsAgainst;

    @Column(nullable = false)
    private int points;
}
