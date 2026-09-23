-- Blasons Bosnie recuperes via TheSportsDB.
UPDATE team SET logo_path = '956.png' WHERE id = 956; -- HSK ZRINJSKI MOSTAR -> Zrinjski Mostar
-- SLOGA MERIDIAN -> "Sloga Doboj" : seul club "Sloga" de Bosnian Premier Liga sur
-- TheSportsDB ("Meridian" est un nom de sponsor courant pour ce club dans les Balkans) ;
-- pas de correspondance exacte "Sloga Meridian" distincte trouvee.
UPDATE team SET logo_path = '522.png' WHERE id = 522;
