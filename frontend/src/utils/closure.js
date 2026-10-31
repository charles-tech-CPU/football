// Cloture de fin de saison (panneau ClosurePanel) : fonctions pures, sans etat Vue.
import { summarizeTie } from './cupTies.js'
import { isDecided } from './matchEdit.js'

function isPendingName(name) {
  return (name ?? '').toUpperCase().startsWith('A DETERMINER')
}

// Classement final propose : les mini-championnats dans l'ordre (groupe du haut d'abord), puis les
// equipes classees qui n'y figurent pas (ex: Gibraltar, les 5 derniers n'ont pas de 2e phase), dans
// l'ordre du classement general. Seules les equipes du classement general sont gardees.
export function proposedRanking(standings, groups = []) {
  const known = new Map(standings.map(r => [r.teamId, r]))
  const result = []
  const seen = new Set()
  for (const row of [...groups.flatMap(g => g.rows), ...standings]) {
    if (!known.has(row.teamId) || seen.has(row.teamId)) continue
    seen.add(row.teamId)
    result.push(known.get(row.teamId))
  }
  return result
}

// Deplace la ligne d'index "from" de "delta" crans (-1 = vers le haut), sans sortir de la liste.
export function moveRow(rows, from, delta) {
  const to = from + delta
  if (to < 0 || to >= rows.length) return rows
  const moved = rows.slice()
  const [row] = moved.splice(from, 1)
  moved.splice(to, 0, row)
  return moved
}

// Matchs restant a jouer (barrages compris), hors places "A DETERMINER" d'un tirage a venir.
export function pendingCount(matches) {
  return matches.filter(m => !isDecided(m) && !isPendingName(m.team1Name) && !isPendingName(m.team2Name)).length
}

// Clubs ayant joue au moins un match de la competition (choix du vainqueur d'une coupe).
export function teamsOf(matches) {
  const byId = new Map()
  for (const m of matches) {
    if (!isPendingName(m.team1Name)) byId.set(m.team1Id, m.team1Name)
    if (!isPendingName(m.team2Name)) byId.set(m.team2Id, m.team2Name)
  }
  return [...byId].map(([id, name]) => ({ id, name })).sort((a, b) => a.name.localeCompare(b.name))
}

// Finale d'une coupe : la confrontation du tour du match le plus tardif. Vainqueur et finaliste
// (null tant que la finale n'est pas decidee ou si ce dernier tour compte plusieurs confrontations).
export function cupFinal(matches) {
  const dated = matches.filter(m => m.date)
  if (!dated.length) return { winnerId: null, runnerUpId: null }
  const last = dated.reduce((a, b) => ((b.date ?? '') > (a.date ?? '') ? b : a))
  const legs = matches.filter(m => m.roundLabel === last.roundLabel)
  const pair = new Set([last.team1Id, last.team2Id])
  if (legs.some(m => !pair.has(m.team1Id) || !pair.has(m.team2Id))) return { winnerId: null, runnerUpId: null }
  const { winnerId, teamAId, teamBId } = summarizeTie(legs)
  if (winnerId == null) return { winnerId: null, runnerUpId: null }
  return { winnerId, runnerUpId: winnerId === teamAId ? teamBId : teamAId }
}
