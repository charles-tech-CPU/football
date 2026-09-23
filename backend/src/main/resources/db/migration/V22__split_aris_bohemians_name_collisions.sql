-- ARIS (id 673, pays "Grece") et BOHEMIANS (id 7, pays "Eire") ne sont pas des doublons
-- d'un meme club a fusionner (cf. V17/V18/V20) : ce sont deux clubs reels distincts qui
-- partagent le meme nom dans deux pays differents (Aris Thessalonique / Aris Limassol,
-- Bohemian FC Dublin / Bohemians 1905 Prague). L'import a dedoublonne les equipes par nom
-- exact, fusionnant par erreur les deux calendriers sous une seule fiche. On separe le
-- calendrier du "mauvais" pays (Chypre / Tchequie) vers une nouvelle fiche dediee, avec
-- un nom complet non ambigu (contrainte UNIQUE sur team.name).

INSERT INTO team (name, country, logo_path) VALUES ('ARIS LIMASSOL', 'Chypre', 'aris_limassol.png');
INSERT INTO team (name, country, logo_path) VALUES ('BOHEMIANS 1905', 'Tchequie', 'bohemians_1905.png');

UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'ARIS LIMASSOL')
WHERE team1_id = 673 AND competition_id = (SELECT id FROM competition WHERE code = 'CHYPRE' AND season = 2027);
UPDATE match SET team2_id = (SELECT id FROM team WHERE name = 'ARIS LIMASSOL')
WHERE team2_id = 673 AND competition_id = (SELECT id FROM competition WHERE code = 'CHYPRE' AND season = 2027);

UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'BOHEMIANS 1905')
WHERE team1_id = 7 AND competition_id = (SELECT id FROM competition WHERE code = 'TCHEQUIE' AND season = 2027);
UPDATE match SET team2_id = (SELECT id FROM team WHERE name = 'BOHEMIANS 1905')
WHERE team2_id = 7 AND competition_id = (SELECT id FROM competition WHERE code = 'TCHEQUIE' AND season = 2027);
