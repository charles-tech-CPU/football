// Matchs hors groupe d'une competition de selections (barrages de promotion / relegation, play-in,
// quarts, final four...) : fonctions pures du composant InternationalKnockoutRounds.
import { summarizeTie } from './cupTies.js'

// Groupe d'un match : celui qu'il porte (historique, cf. V115) ou, a defaut, le groupe commun de
// ses deux equipes d'apres le classement ; undefined pour un match hors groupe.
export function matchGroupResolver(matches, rows) {
  const groupByTeam = new Map(rows.map(r => [r.teamId, r.group ?? null]))
  const groupsOnMatches = matches.some(m => m.groupName)
  return m => {
    if (groupsOnMatches) return m.groupName ?? undefined
    const g1 = groupByTeam.get(m.team1Id)
    return g1 !== undefined && g1 === groupByTeam.get(m.team2Id) ? g1 : undefined
  }
}

const LEG = / - (Aller|Retour)$/

// "Barrages de promotion et relégation - Aller" -> "Barrages de promotion et relégation"
export function roundName(label) {
  return (label ?? 'Autres matchs').replace(LEG, '')
}

function byDate(a, b) {
  return (a.date ?? '9999').localeCompare(b.date ?? '9999') || (a.time ?? '').localeCompare(b.time ?? '')
}

// Tours hors groupe dans l'ordre chronologique, chacun avec ses confrontations (aller et retour
// regroupes : score cumule et qualifie quand la confrontation est terminee).
export function knockoutRounds(matches) {
  const rounds = new Map()
  for (const m of matches.toSorted(byDate)) {
    const name = roundName(m.roundLabel)
    if (!rounds.has(name)) rounds.set(name, new Map())
    const pair = [m.team1Id, m.team2Id].sort((a, b) => a - b).join('-')
    const ties = rounds.get(name)
    if (!ties.has(pair)) ties.set(pair, [])
    ties.get(pair).push(m)
  }
  return [...rounds.entries()].map(([name, ties]) => {
    const list = [...ties.values()].map(legs => ({ ...summarizeTie(legs), twoLegged: legs.length > 1 }))
    return {
      name,
      ties: list,
      played: list.flatMap(t => t.legs).filter(m => m.score1 != null && m.score2 != null).length,
      total: list.flatMap(t => t.legs).length
    }
  })
}

// Tours proposes a l'ajout d'un match, selon la competition (libelles deja utilises par les imports).
const NATIONS_LEAGUE_ROUNDS = [
  'Quarts de finale',
  'Barrages de promotion et relégation',
  'Demi-finales',
  'Match pour la 3e place',
  'Finale'
]

export function roundSuggestions(code) {
  if ((code ?? '').startsWith('CONCACAF_NL')) return ['Play-in', ...NATIONS_LEAGUE_ROUNDS]
  if ((code ?? '').includes('_NL_')) return NATIONS_LEAGUE_ROUNDS
  return ['Barrages', 'Huitièmes de finale', 'Quarts de finale', 'Demi-finales', 'Match pour la 3e place', 'Finale']
}

// Libelle enregistre : tour + manche ("Quarts de finale - Aller"), sans manche pour un match unique.
export function roundLabel(round, leg) {
  const name = roundName(round.trim())
  return leg ? `${name} - ${leg}` : name
}

// "Ligue A - Groupe 1" -> "A1", "Ligue B" -> "B" ; sinon le libelle tel quel (abrege par l'appelant).
export function leagueTag(group) {
  if (!group) return ''
  const m = /^Ligue\s+([A-Z])\b(?:.*?(\d+))?/i.exec(group)
  return m ? `${m[1].toUpperCase()}${m[2] ?? ''}` : group
}
