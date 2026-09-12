-- "OLYMPIQUE LYONNAIS" (utilise en LDC) et "LYON" (championnat France) sont le
-- meme club sous deux noms differents (cf. limitation documentee dans le
-- README sur les doublons d'equipes). On reutilise le meme logo plutot que
-- d'en re-telecharger un.
UPDATE team SET logo_path = (SELECT logo_path FROM team WHERE id = 687) WHERE id = 863;
