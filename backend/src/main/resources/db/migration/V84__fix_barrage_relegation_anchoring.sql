-- Correction : le systeme d'icones automatique place TOUJOURS le barrage juste au-dessus de
-- la zone de relegation (bloc contigu en bas de classement). En fixant barrageSlots sans
-- relegationSlots coherent pour Roumanie/Russie/Portugal/Suede (V72/V73/V74/V81), le barrage
-- se serait affiche sur les mauvais rangs (ex: 15e/16e au lieu de 13e/14e pour la Roumanie a
-- 16 equipes). Ajuste relegationSlots pour ancrer le barrage sur le bon rang, en supposant
-- que les rangs juste en dessous du barrage sont directement relegues (schema le plus courant
-- observe sur tous les autres championnats de ce projet) - a confirmer si incorrect.
UPDATE competition SET relegation_slots = 2 WHERE code = 'ROUMANIE' AND season = 2027; -- barrage 13e/14e, relegation 15e/16e (16 equipes)
UPDATE competition SET relegation_slots = 2 WHERE code = 'RUSSIE' AND season = 2027; -- barrage 13e/14e, relegation 15e/16e (16 equipes)
UPDATE competition SET relegation_slots = 2 WHERE code = 'PORTUGAL' AND season = 2027; -- barrage 16e, relegation 17e/18e (18 equipes)
UPDATE competition SET relegation_slots = 2 WHERE code = 'SUEDE' AND season = 2027; -- barrage 14e, relegation 15e/16e (16 equipes)
