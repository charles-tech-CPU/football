-- Quarts et demies inseres en double (8 places generiques au lieu de 4 pour les quarts, 4 au
-- lieu de 2 pour les demies), meme bug que Gibraltar : tout encore en placeholders "a
-- determiner" (pas d'equipes reelles), un des 2 lots est supprime arbitrairement.
DELETE FROM match WHERE id IN (12055, 12056, 12057, 12058); -- doublon quarts
DELETE FROM match WHERE id IN (12169, 12170); -- doublon demies

-- Blason Moldavie recupere via TheSportsDB.
UPDATE team SET logo_path = '582.png' WHERE id = 582; -- POLITEHCNICA UTM -> Politehnica UTM
