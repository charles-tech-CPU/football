// Inversion domicile / exterieur d'un match de championnat (cf. SwapHomeButton) : l'icone
// n'apparait que sur les matchs de championnat hors barrages, et on propose d'inverser aussi
// les matchs "retour" (memes clubs, a l'envers, meme competition).
import { isPlayoffMatch } from './playoffRounds.js'

export function canSwapHomeAway(match) {
  return match.competitionType === 'LEAGUE' && !isPlayoffMatch(match)
}

const DAY_MS = 24 * 60 * 60 * 1000

function dayDistance(a, b) {
  if (!a || !b) return Number.POSITIVE_INFINITY
  return Math.abs(Date.parse(a) - Date.parse(b)) / DAY_MS
}

// Matchs retour de `match` parmi `matches`, le plus proche en date d'abord (sans date en dernier).
export function reverseFixtures(matches, match) {
  return matches
    .filter(m => m.id !== match.id && m.team1Id === match.team2Id && m.team2Id === match.team1Id)
    .sort((a, b) => dayDistance(a.date, match.date) - dayDistance(b.date, match.date) || (a.date ?? '').localeCompare(b.date ?? ''))
}
