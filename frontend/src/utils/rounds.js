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
