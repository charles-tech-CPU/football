package com.charles.footresults.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un club. "country" est un champ texte libre (comme dans le fichier Excel
 * source), pas une reference stricte vers une table Pays : les feuilles de
 * calendrier national et de coupes d'Europe n'utilisent pas exactement les
 * memes libelles de pays (noms complets vs codes courts), donc on evite une
 * table de correspondance couteuse pour la v1. A revoir si besoin d'un
 * referentiel pays plus rigoureux (drapeaux, tri, etc.).
 */
@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 60)
    private String country;

    public Team(String name, String country) {
        this.name = name;
        this.country = country;
    }
}
