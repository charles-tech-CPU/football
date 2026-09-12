-- Doublons crees par des abreviations/fautes de frappe lors de la saisie
-- Excel (identifies avec l'utilisateur via le contexte des matchs) : fusionnes
-- dans TeamService.merge avant cette migration (reaffectation des matchs puis
-- suppression du doublon), sauf PENYA dont il ne restait qu'a recuperer le
-- logo une fois son identite reelle connue.
-- EVA (Estonie) = FC LEVADIA TALLINN ; JEG (Lettonie) = JELGAVA ;
-- ATN (Belgique) = ANTWERP ; RAL (Belgique) = RAAL LA LOUVIERE ;
-- EN (Andorre) = PENYA (Penya Encarnada d'Andorra).
UPDATE team SET logo_path = '812.png' WHERE id = 812;
