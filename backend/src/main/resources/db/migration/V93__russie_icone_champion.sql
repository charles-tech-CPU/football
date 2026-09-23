-- Russie exclue des competitions europeennes (ldc/el/ecl_slots a 0) : le 1er n'affichait
-- donc aucune icone de champion (le marqueur automatique 🏆 est conditionne a ldc_slots > 0).
-- ldc_slots = 1 sans 2e place LDC (la boucle 🔷 ne se declenche qu'a partir de 2) affiche
-- juste l'icone champion, sans impliquer une vraie qualification europeenne.
UPDATE competition SET ldc_slots = 1 WHERE code = 'RUSSIE' AND season = 2027;
