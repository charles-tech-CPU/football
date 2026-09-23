// Resume d'une confrontation a elimination directe (1 match sec ou aller-retour) : aggregat,
// tirs au but eventuels et vainqueur. "A" designe l'equipe qui recoit au premier match.
export function summarizeTie(legs) {
  const sorted = legs.toSorted((a, b) => (a.date ?? '').localeCompare(b.date ?? ''))
  const first = sorted[0]
  const teamAId = first.team1Id
  const teamBId = first.team2Id
  const { aggA, aggB, hasAllScores } = aggregate(sorted, teamAId)

  // Tirs au but : uniquement pertinents si l'aggregat (ou le match unique) est a egalite -
  // portes par le dernier match joue (celui qui a effectivement ete suivi de la seance).
  const decider = sorted.at(-1)
  const aggTied = hasAllScores && aggA === aggB
  const { penA, penB } = aggTied ? penaltiesOf(decider, teamAId) : NO_PENALTIES
  const wentToPenalties = penA != null && penB != null

  let winnerId = null
  if (wentToPenalties) {
    winnerId = penA > penB ? teamAId : teamBId
  } else if (hasAllScores && aggA !== aggB) {
    winnerId = aggA > aggB ? teamAId : teamBId
  }
  return {
    teamAId,
    teamAName: first.team1Name,
    teamBId,
    teamBName: first.team2Name,
    legs: sorted,
    aggA,
    aggB,
    hasAllScores,
    winnerId,
    decider,
    needsPenalty: aggTied && !wentToPenalties,
    wentToPenalties,
    penA,
    penB
  }
}

const NO_PENALTIES = { penA: null, penB: null }

// Buts cumules de A et B sur les manches deja jouees ; hasAllScores = toutes les manches jouees.
function aggregate(legs, teamAId) {
  let aggA = 0
  let aggB = 0
  let hasAllScores = true
  for (const leg of legs) {
    if (leg.score1 == null || leg.score2 == null) {
      hasAllScores = false
    } else {
      const aAtHome = leg.team1Id === teamAId
      aggA += aAtHome ? leg.score1 : leg.score2
      aggB += aAtHome ? leg.score2 : leg.score1
    }
  }
  return { aggA, aggB, hasAllScores }
}

function penaltiesOf(match, teamAId) {
  if (match.penaltyScore1 == null || match.penaltyScore2 == null) return NO_PENALTIES
  return match.team1Id === teamAId
    ? { penA: match.penaltyScore1, penB: match.penaltyScore2 }
    : { penA: match.penaltyScore2, penB: match.penaltyScore1 }
}
