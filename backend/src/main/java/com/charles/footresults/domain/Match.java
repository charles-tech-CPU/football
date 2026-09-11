package com.charles.footresults.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Un match de football (un seul match, pas de "best of" comme au LoL).
 * score1/score2 = buts marques. Le classement (points/V-N-D/buts) n'est
 * jamais stocke : il est recalcule depuis les matchs COMPLETED d'une
 * competition de type LEAGUE (voir StandingsService).
 *
 * "date" est nullable : les tours de qualification des coupes d'Europe
 * n'ont pas de date exploitable dans le fichier source (seul le score
 * cumule aller/retour est connu), donc on l'autorise a etre absent plutot
 * que d'inventer une date.
 */
@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    /**
     * Libelle libre : "J1".."J38" pour une journee de championnat,
     * "1ER TOUR QUALIF - Aller", "8E DE FINALE"... pour une coupe.
     */
    @Column(name = "round_label", nullable = false, length = 60)
    private String roundLabel;

    private LocalDate date;

    private LocalTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    /** Buts marques par team1. Null tant que le match n'est pas joue. */
    private Integer score1;

    /** Buts marques par team2. Null tant que le match n'est pas joue. */
    private Integer score2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.SCHEDULED;
}
