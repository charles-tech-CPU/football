-- "GRE" (824) est en realite GROSUPLJE (228), tronque sur 1 de ses 36 matchs (championnat en
-- quadruple aller-retour, 9 adversaires x4) : confirme, NK ALUMINIJ-GROSUPLJE n'avait que 3
-- confrontations au lieu de 4, et GROSUPLJE n'avait que 35 matchs au lieu de 36.
UPDATE match SET team2_id = 228 WHERE id = 8628;
DELETE FROM team WHERE id = 824;

-- Blason Slovenie recupere via TheSportsDB.
UPDATE team SET logo_path = '235.png' WHERE id = 235; -- O. LJUBLJANA -> Olimpija Ljubljana
