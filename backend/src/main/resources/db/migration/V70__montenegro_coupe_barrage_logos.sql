-- Demies inserees en double (4 places generiques au lieu de 2), meme bug que Gibraltar/
-- Moldavie : tout encore en placeholders "a determiner", un des 2 lots est supprime.
DELETE FROM match WHERE id IN (12124, 12125); -- doublon demies

-- Blasons Montenegro recuperes via TheSportsDB.
UPDATE team SET logo_path = '477.png' WHERE id = 477; -- BIUDUCNOST -> Buducnost Podgorica
UPDATE team SET logo_path = '462.png' WHERE id = 462; -- MLADOST DG -> Mladost Donja Gorica

-- Icones : barrage deja en place pour le 8e et le 9e (aller-retour contre le 3e/2e de
-- Division 2), 10e relegue directement.
UPDATE competition SET barrage_slots = 2, relegation_slots = 1 WHERE code = 'MONTENEGRO' AND season = 2027;
