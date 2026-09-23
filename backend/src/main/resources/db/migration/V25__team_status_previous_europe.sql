-- Competition europeenne jouee par le club la saison precedente ("EL" ou "ECL"), affichee
-- comme couleur de ligne dans le classement (au meme titre que champion sortant/vainqueur
-- de coupe/promu) - distincte des places qualificatives de la saison EN COURS (calculees
-- par rang, affichees en icone).
ALTER TABLE team_competition_status ADD COLUMN previous_europe_competition VARCHAR(10);
