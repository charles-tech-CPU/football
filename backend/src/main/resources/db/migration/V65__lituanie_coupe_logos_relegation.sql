-- Blasons Lituanie recuperes via TheSportsDB.
UPDATE team SET logo_path = '12.png' WHERE id = 12; -- DZIUGAS TELISAI -> Dziugas Telsiai
UPDATE team SET logo_path = '734.png' WHERE id = 734; -- FA SAUILIAI -> FA Siauliai

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours :
-- reconstitue a partir des dates/vainqueurs (8 equipes en huitiemes -> quarts -> demies ->
-- finale, tout en match unique). 2 tours se sont joues a egalite (DZIUGAS TELISAI-BANGA 0-0
-- en huitiemes, DAINAVA ALYTUS-TRANSINVEST 1-1 en quarts) et ont ete qualifies aux tirs au
-- but d'apres la suite du tableau : scores de tab non connus, a ajouter via le controle
-- "+ Tirs au but" du bracket.
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (819, 820, 821, 822, 825, 829, 830, 837);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (1145, 1148, 1152, 1153);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (2884, 2918);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 4394;

-- Icone : 10e (dernier) relegue.
UPDATE competition SET relegation_slots = 1 WHERE code = 'LITUANIE' AND season = 2027;
