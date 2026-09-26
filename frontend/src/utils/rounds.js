// Numero de journee extrait du libelle ("J12" -> 12, "Journee 3" -> 3), null s'il n'y en a pas.
export function roundNumber(m) {
  const found = (m.roundLabel ?? '').match(/(\d+)/)
  return found ? Number.parseInt(found[1], 10) : null
}

// Ordre chronologique des matchs : numero de journee, puis date (sans date en premier), puis id.
export function compareByRoundThenDate(a, b) {
  const ra = roundNumber(a)
  const rb = roundNumber(b)
  if (ra != null && rb != null && ra !== rb) return ra - rb
  const ad = a.date ?? ''
  const bd = b.date ?? ''
  if (ad !== bd) return ad.localeCompare(bd)
  return a.id - b.id
}

// Libelles de tour du plus recent au plus ancien, dates par la premiere date du tour : un match
// en retard joue bien apres ne fait pas remonter sa journee d'origine. Tours sans date en dernier.
export function roundsByRecency(matches) {
  const start = new Map()
  for (const m of matches) {
    const current = start.get(m.roundLabel)
    if (!start.has(m.roundLabel) || (m.date != null && (current == null || m.date < current))) {
      start.set(m.roundLabel, m.date ?? null)
    }
  }
  return [...start].sort(([, a], [, b]) => (b ?? '').localeCompare(a ?? '')).map(([label]) => label)
}
