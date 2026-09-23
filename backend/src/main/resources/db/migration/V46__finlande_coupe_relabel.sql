-- Structure deja complete et coherente (16 equipes en 1/8e -> quarts -> demies -> finale,
-- confirme par la progression des vainqueurs), seul le round_label generique "Coupe
-- nationale" ne distinguait pas les tours.
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (918, 920, 921, 922, 923, 925, 926, 932);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (1002, 1003, 1004, 1005);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (1215, 1218);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 3047;
