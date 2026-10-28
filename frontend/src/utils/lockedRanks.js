// Places definitives d'un classement : une equipe ne peut plus monter ni descendre, quels que
// soient les matchs restants. Meme regle prudente que le classement projete cote backend
// (StandingsService.Outlook.alwaysAbove) : chaque equipe est traitee independamment (pire cas =
// 0 point, meilleur cas = toutes victoires), donc une place annoncee definitive l'est vraiment ;
// l'inverse n'est pas garanti (deux poursuivants qui doivent s'affronter ne peuvent pas gagner
// tous les deux).
import { leagueRound } from './competitionProgress.js'

const POINTS_WIN = 3

// Nombre de matchs de championnat (journees "J12") encore a jouer par equipe : matchs saisis pas
// encore joues (un forfait ne rapporte pas de point), plus les journees attendues pas encore
// saisies (totalRounds), une par equipe. Les barrages entre places "a determiner" ne changent pas
// le classement ; un mini-championnat a determiner (Albanie) si : ses matchs redistribuent des
// points, d'ou un match par place concernee ajoute a chaque equipe faute de savoir qui le jouera.
export function remainingByTeam(matches, teamIds, totalRounds) {
  const remaining = new Map(teamIds.map(id => [id, 0]))
  const lastCreated = Math.max(0, ...matches.map(leagueRound).filter(n => n != null))
  const missing = Math.max(0, (totalRounds ?? 0) - lastCreated)
  let pendingMiniLeague = 0
  for (const m of matches) {
    if (m.status === 'COMPLETED' || m.status === 'FORFEIT') continue
    if (leagueRound(m) != null) {
      for (const id of [m.team1Id, m.team2Id]) {
        if (remaining.has(id)) remaining.set(id, remaining.get(id) + 1)
      }
    } else if ((m.roundLabel ?? '').startsWith('Mini-championnat')) {
      pendingMiniLeague++
    }
  }
  const extra = missing + pendingMiniLeague
  for (const id of teamIds) remaining.set(id, remaining.get(id) + extra)
  return remaining
}

// Ids des equipes dont la place est definitive. rows : classement trie (points, puis criteres de
// departage) ; remaining : Map teamId -> matchs restants, une equipe absente (ou Infinity) n'est
// jamais definitive. A egalite de points, seul un classement termine pour les deux equipes
// departage (difference de buts...) : sinon des buts restent a marquer.
export function lockedTeamIds(rows, remaining) {
  const left = row => remaining.get(row.teamId) ?? Infinity
  const alwaysAbove = (a, ia, b, ib) => {
    const maxB = b.points + POINTS_WIN * left(b)
    if (a.points !== maxB) return a.points > maxB
    return left(a) === 0 && left(b) === 0 && ia < ib
  }
  const locked = new Set()
  rows.forEach((row, i) => {
    if (!Number.isFinite(left(row))) return
    const fixed = rows.every((other, j) => {
      if (j === i) return true
      return j < i ? alwaysAbove(other, j, row, i) : alwaysAbove(row, i, other, j)
    })
    if (fixed) locked.add(row.teamId)
  })
  return locked
}
