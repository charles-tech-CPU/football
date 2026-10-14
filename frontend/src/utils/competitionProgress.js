// Etat d'avancement d'une competition pour les cards de l'onglet Competitions :
// matchs en retard et journees dont le calendrier (date + horaire) est complet.
import { isPlayed } from './matchEdit.js'

const DELAYED_STATUSES = new Set(['POSTPONED', 'SUSPENDED'])

// Match en retard = pas encore joue, et reporte/suspendu ou date passee.
// Dates au format ISO "AAAA-MM-JJ", comparables comme des chaines.
export function isLate(m, today) {
  if (isPlayed(m) || m.status === 'FORFEIT') return false
  if (DELAYED_STATUSES.has(m.status)) return true
  return m.date != null && m.date < today
}

export function lateCount(matches, today) {
  return matches.filter(m => isLate(m, today)).length
}

// Numero de journee de championnat ("J12" -> 12) ; null pour les barrages, mini-championnats...
export function leagueRound(m) {
  const found = (m.roundLabel ?? '').match(/^J(\d+)$/)
  return found ? Number.parseInt(found[1], 10) : null
}

// Match programme = date et horaire renseignes. Un match joue n'en a plus besoin, un match
// reporte/suspendu attend sa reprogrammation (deja compte dans les matchs en retard).
export function isScheduled(m) {
  if (isPlayed(m) || m.status === 'FORFEIT' || DELAYED_STATUSES.has(m.status)) return true
  return m.date != null && m.time != null
}

// Avancement du calendrier sur les journees attendues (totalRounds, a defaut la derniere
// journee saisie) : une journee est complete si elle existe et que tous ses matchs sont
// programmes. firstMissing = premiere journee incomplete ou absente (null si tout est complet),
// firstUntimedDate = date la plus proche d'un match date mais encore sans horaire (null si aucun).
export function calendarProgress(matches, totalRounds) {
  const rounds = new Map()
  let firstUntimedDate = null
  for (const m of matches) {
    const n = leagueRound(m)
    if (n == null) continue
    const scheduled = isScheduled(m)
    rounds.set(n, (rounds.get(n) ?? true) && scheduled)
    if (!scheduled && m.date != null && (firstUntimedDate == null || m.date < firstUntimedDate)) {
      firstUntimedDate = m.date
    }
  }
  const total = totalRounds ?? Math.max(0, ...rounds.keys())
  let complete = 0
  let firstMissing = null
  for (let n = 1; n <= total; n++) {
    if (rounds.get(n)) complete++
    else firstMissing ??= n
  }
  return { total, complete, firstMissing, firstUntimedDate }
}

// Ce qu'il reste a jouer : journees de championnat ayant encore un match a jouer (hors matchs en
// retard, deja signales a part) ou pas encore saisies jusqu'a totalRounds, et autres tours
// (barrages, poules de 2e phase...) encore a jouer. Les journees de 2e phase reprennent parfois
// les numeros J1, J2... : c'est bien la presence d'un match a jouer qui compte, pas le numero.
// finished = plus aucun match a jouer (y compris en retard) ni journee manquante.
export function remainingProgress(matches, totalRounds, today) {
  const pending = matches.filter(m => !isPlayed(m) && m.status !== 'FORFEIT')
  const lastCreated = Math.max(0, ...matches.map(leagueRound).filter(n => n != null))
  const rounds = new Set()
  const otherRounds = new Set()
  for (const m of pending) {
    if (isLate(m, today)) continue
    const n = leagueRound(m)
    if (n == null) otherRounds.add(m.roundLabel)
    else rounds.add(n)
  }
  const missing = Math.max(0, (totalRounds ?? 0) - lastCreated)
  return {
    rounds: rounds.size + missing,
    otherRounds: otherRounds.size,
    finished: pending.length === 0 && missing === 0
  }
}
