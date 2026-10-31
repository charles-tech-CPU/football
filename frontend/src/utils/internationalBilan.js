// Bilan historique d'une competition de selections (composant InternationalBilan) : fonctions pures.

const JOURNEY_COLUMNS = [
  { key: 'titles', label: '🏆 Titres', title: 'Vainqueur' },
  { key: 'runnerUps', label: 'Finales', title: 'Finaliste (finale perdue)' },
  { key: 'semiFinals', label: 'Demies', title: 'Demi-finale (ou match pour la 3e place)' },
  { key: 'quarterFinals', label: 'Quarts', title: 'Quart de finale' },
  { key: 'roundOf16', label: '8es', title: 'Huitième de finale' },
  { key: 'roundOf32', label: '16es', title: 'Seizième de finale' },
  { key: 'groupStage', label: 'Poules', title: 'Phase de groupes ou tour préliminaire' }
]

// Colonnes du parcours utiles pour cette competition (pas de 16es avant la Coupe du monde 2026...).
export function journeyColumnsFor(journeys) {
  return JOURNEY_COLUMNS.filter(c => journeys.some(j => j[c.key] > 0))
}

function normalize(text) {
  return (text ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

// Lignes dans l'ordre recu, numerotees avant filtrage (le rang reste celui du classement complet).
export function rankedRows(rows, search = '') {
  const q = normalize(search.trim())
  return rows
    .map((row, i) => ({ ...row, rank: i + 1 }))
    .filter(row => !q || normalize(row.teamName).includes(q))
}
