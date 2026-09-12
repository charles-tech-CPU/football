-- Les dates de janvier de la grille croisee "phase de ligue" LDC (feuille
-- Excel) etaient enregistrees en 2026 au lieu de 2027 (erreur de millesime
-- dans le fichier source : la saison se termine en janvier de la 2e annee
-- civile, pas de la 1ere). Corrige les 18 matchs concernes (V6).
UPDATE match
SET date = date + INTERVAL '1 year'
WHERE competition_id = (SELECT id FROM competition WHERE code = 'LDC' AND season = 2027)
  AND round_label = 'PHASE DE LIGUE'
  AND date BETWEEN '2026-01-01' AND '2026-01-31';
