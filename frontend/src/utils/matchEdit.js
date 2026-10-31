// Edition "en ligne" d'un match dans un tableau (calendriers, matchs reportes, matchs a venir
// d'une competition) : fonctions pures partagees par les ecrans, sans etat Vue.
import { formatTime } from './format.js'

const MANUAL_STATUSES = new Set(['POSTPONED', 'SUSPENDED', 'FORFEIT'])

const STATUS_ROW_CLASSES = {
  POSTPONED: 'row-postponed',
  SUSPENDED: 'row-suspended',
  FORFEIT: 'row-forfeit'
}

export function statusRowClass(status) {
  return STATUS_ROW_CLASSES[status] ?? ''
}

// Couleur de ligne : statut force (reporte/suspendu/forfait) en priorite, sinon match en retard
// (date passee) ou du jour. Dates au format ISO "AAAA-MM-JJ", comparables comme des chaines.
export function rowClassFor(status, date, today) {
  const cls = statusRowClass(status)
  if (cls || !date) return cls
  if (date < today) return 'row-overdue'
  if (date === today) return 'row-today'
  return ''
}

// Match joue = score saisi des 2 cotes (quel que soit le statut, forfait compris).
export function isPlayed(m) {
  return m.score1 != null && m.score2 != null
}

// Resultat acquis, comptant au classement : match joue ou gagne sur tapis vert (forfait avec son
// score). Un forfait sans score (equipe forfait pas encore designee) reste a completer.
export function isDecided(m) {
  return (m.status === 'COMPLETED' || m.status === 'FORFEIT') && isPlayed(m)
}

// Score sur tapis vert : 3-0 pour l'equipe qui n'abandonne pas.
export const FORFEIT_GOALS = 3

// Valeur du menu de statut : un forfait se choisit par equipe ("FORFEIT_1" = l'equipe 1
// abandonne), deduite du score sur tapis vert ; "FORFEIT" seul = equipe forfait pas encore designee.
export function statusChoice(edit) {
  if (edit.status !== 'FORFEIT') return edit.status
  if (!isScore(edit.score1) || !isScore(edit.score2) || edit.score1 === edit.score2) return 'FORFEIT'
  return edit.score1 < edit.score2 ? 'FORFEIT_1' : 'FORFEIT_2'
}

// Statut et score correspondant a un choix du menu : un forfait remplit le score sur tapis vert,
// quitter un forfait efface ce score (le match redevient a jouer).
export function applyStatusChoice(edit, choice) {
  if (choice === 'FORFEIT_1') return { status: 'FORFEIT', score1: 0, score2: FORFEIT_GOALS }
  if (choice === 'FORFEIT_2') return { status: 'FORFEIT', score1: FORFEIT_GOALS, score2: 0 }
  const leavingForfeit = edit.status === 'FORFEIT' && choice !== 'FORFEIT'
  return {
    status: choice,
    score1: leavingForfeit ? null : edit.score1,
    score2: leavingForfeit ? null : edit.score2
  }
}

// Valeurs editables d'un match (champs de formulaire : chaines vides plutot que null).
export function editFromMatch(m) {
  return {
    team1Id: m.team1Id,
    team2Id: m.team2Id,
    date: m.date ?? '',
    time: formatTime(m.time),
    score1: m.score1,
    score2: m.score2,
    penaltyScore1: m.penaltyScore1 ?? null,
    penaltyScore2: m.penaltyScore2 ?? null,
    status: MANUAL_STATUSES.has(m.status) ? m.status : ''
  }
}

function isScore(value) {
  return typeof value === 'number' && Number.isInteger(value) && value >= 0
}

// Tirs au but a saisir : match decisif d'une confrontation de coupe (match.knockout, fourni par
// le calendrier) dont le score saisi laisse les 2 equipes a egalite, manches precedentes comprises.
// Jamais sur un forfait : l'equipe qui n'abandonne pas est qualifiee d'office.
export function penaltiesNeeded(match, edit) {
  const ko = match.knockout
  if (!ko?.decider || edit.status === 'FORFEIT' || !isScore(edit.score1) || !isScore(edit.score2)) return false
  return edit.score1 + ko.otherLegsGoals1 === edit.score2 + ko.otherLegsGoals2
}

// Corps de la requete PUT /api/matches/{id} a partir des valeurs editees. Les tirs au but deja
// saisis sont conserves ; sur un match de coupe du calendrier, ils sont effaces des que le
// score ne laisse plus les equipes a egalite.
export function matchPayload(match, edit) {
  const keepPenalties = !match.knockout || penaltiesNeeded(match, edit)
  return {
    competitionId: match.competitionId,
    roundLabel: match.roundLabel,
    date: edit.date || null,
    time: edit.time || null,
    team1Id: edit.team1Id,
    team2Id: edit.team2Id,
    score1: edit.score1,
    score2: edit.score2,
    penaltyScore1: keepPenalties ? edit.penaltyScore1 ?? null : null,
    penaltyScore2: keepPenalties ? edit.penaltyScore2 ?? null : null,
    status: edit.status || null
  }
}

// Lignes "Equipe 1 : ancien -> nouveau" decrivant un changement d'equipe (vide si aucun).
export function teamChangeLines(match, edit, teamNameById) {
  const lines = []
  if (edit.team1Id !== match.team1Id) lines.push(`Équipe 1 : ${match.team1Name} → ${teamNameById(edit.team1Id)}`)
  if (edit.team2Id !== match.team2Id) lines.push(`Équipe 2 : ${match.team2Name} → ${teamNameById(edit.team2Id)}`)
  return lines
}

// Clubs proposes pour un match : ceux du pays de sa competition (championnats/coupes nationales),
// tous pour une competition sans pays (coupes d'Europe). L'equipe actuellement choisie reste
// toujours proposee, meme si son pays ne correspond pas exactement (libelles en texte libre).
export function teamOptionsFor(teams, country, currentId) {
  let list = country ? teams.filter(t => t.country?.toLowerCase() === country.toLowerCase()) : teams
  if (currentId != null && !list.some(t => t.id === currentId)) {
    const current = teams.find(t => t.id === currentId)
    if (current) list = [...list, current]
  }
  return list.toSorted((a, b) => a.name.localeCompare(b.name))
}

// Message de confirmation avant la suppression d'un match (ex: doublon saisi deux fois).
export function deleteConfirmMessage(match) {
  const score = isPlayed(match) ? ` (${match.score1}-${match.score2})` : ''
  return `Supprimer définitivement le match ${match.team1Name} – ${match.team2Name}${score}, ${match.roundLabel} ?`
}
