// Edition "en ligne" d'un match dans un tableau (calendriers, matchs reportes, matchs a venir
// d'une competition) : fonctions pures partagees par les ecrans, sans etat Vue.
import { formatTime } from './format.js'

const MANUAL_STATUSES = ['POSTPONED', 'SUSPENDED', 'FORFEIT']

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

// Valeurs editables d'un match (champs de formulaire : chaines vides plutot que null).
export function editFromMatch(m) {
  return {
    team1Id: m.team1Id,
    team2Id: m.team2Id,
    date: m.date ?? '',
    time: formatTime(m.time),
    score1: m.score1,
    score2: m.score2,
    status: MANUAL_STATUSES.includes(m.status) ? m.status : ''
  }
}

// Corps de la requete PUT /api/matches/{id} a partir des valeurs editees.
export function matchPayload(match, edit) {
  return {
    competitionId: match.competitionId,
    roundLabel: match.roundLabel,
    date: edit.date || null,
    time: edit.time || null,
    team1Id: edit.team1Id,
    team2Id: edit.team2Id,
    score1: edit.score1,
    score2: edit.score2,
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
