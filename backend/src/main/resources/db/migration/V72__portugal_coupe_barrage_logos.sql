-- Demies inserees en double (4 places generiques au lieu de 2, 2 par date), meme bug que
-- Gibraltar/Moldavie/Montenegro : tout encore en placeholders "a determiner".
DELETE FROM match WHERE id IN (11877, 12064); -- doublon demies

-- Blasons Portugal recuperes via TheSportsDB.
UPDATE team SET logo_path = '553.png' WHERE id = 553; -- ACADEMICO VISEU -> Academico de Viseu
UPDATE team SET logo_path = '543.png' WHERE id = 543; -- FC PORTO -> Porto
UPDATE team SET logo_path = '966.png' WHERE id = 966; -- SPORTING BRAGA -> Braga
UPDATE team SET logo_path = '984.png' WHERE id = 984; -- TORREENSE -> Torreense

-- Icone : barrage deja en place pour le 16e (aller-retour contre le 3e de Division 2).
UPDATE competition SET barrage_slots = 1 WHERE code = 'PORTUGAL' AND season = 2027;
