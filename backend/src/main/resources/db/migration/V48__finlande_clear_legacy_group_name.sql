-- L'ancien mecanisme de groupe (group_name libre, ex: "Championnat"/"Relegation") n'avait
-- ete renseigne que sur 6 des 12 equipes de Finlande, ce qui cassait le classement general
-- (scinde en 3 blocs "Championnat"/"Relegation"/sans groupe au lieu d'un classement plat).
-- Remplace par le mecanisme generique resultsGroupSplit (CountryView.vue), commun a
-- l'Autriche/Bulgarie/Chypre/Danemark/Ecosse : le classement general redevient plat.
UPDATE team_competition_status SET group_name = NULL
WHERE competition_id = (SELECT id FROM competition WHERE code = 'FINLANDE' AND season = 2027);
