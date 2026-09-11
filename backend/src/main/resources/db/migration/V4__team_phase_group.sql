-- Certains championnats se scindent en deuxieme partie de saison en
-- plusieurs mini-groupes (ex: Finlande - "Mestaruussarja"/groupe
-- Championnat pour le top 6, "Haastajasarja"/groupe Relegation pour le
-- bottom 6 ; format similaire en Ecosse, Autriche, Belgique...). On stocke
-- juste le nom du groupe final de chaque equipe ; le classement de ce
-- groupe est ensuite recalcule a partir de TOUS les matchs entre membres de
-- ce meme groupe (englobe naturellement les confrontations de phase 1 +
-- phase 2), sans avoir besoin de marquer chaque match individuellement.
ALTER TABLE team_competition_status ADD COLUMN group_name VARCHAR(60);
