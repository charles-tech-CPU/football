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

// Une ligne par selection avec ses bilans en phase finale et en qualifications (null si elle n'y a
// jamais joue). Tri par points du cote choisi ('finals' ou 'qualifiers'), puis par l'autre cote ;
// a egalite, l'ordre recu du serveur (differences de buts deja departagees) est garde.
export function mergedRecords(finals, qualifiers, sortBy = 'finals') {
  const byTeam = new Map()
  const order = new Map()
  const add = (rows, key) => rows.forEach((r, i) => {
    if (!byTeam.has(r.teamId)) byTeam.set(r.teamId, { teamId: r.teamId, teamName: r.teamName, finals: null, qualifiers: null })
    byTeam.get(r.teamId)[key] = r
    if (!order.has(`${key}:${r.teamId}`)) order.set(`${key}:${r.teamId}`, i)
  })
  add(finals, 'finals')
  add(qualifiers, 'qualifiers')
  const other = sortBy === 'finals' ? 'qualifiers' : 'finals'
  const position = (row, key) => (row[key] ? order.get(`${key}:${row.teamId}`) : Number.MAX_SAFE_INTEGER)
  return [...byTeam.values()].sort((a, b) =>
    position(a, sortBy) - position(b, sortBy) || position(a, other) - position(b, other))
}
