import { isPlayoffMatch } from './playoffRounds.js'

// Controle du calendrier d'un championnat a format fixe (chaque equipe rencontre toutes les
// autres le meme nombre de fois) : equipes qui n'ont pas le bon nombre de matchs, confrontations
// en double (meme equipe qui recoit le meme adversaire trop souvent) et confrontations manquantes.
// Un resultat saisi sur le mauvais match cree a la fois un doublon et un manque, et fait
// apparaitre une "2e phase" fantome dans la grille de resultats (paire rejouee = nouveau cycle).

function isPendingTeam(name) {
  return (name ?? '').toUpperCase().startsWith('A DETERMINER')
}

// Nombre de matchs le plus frequent : repli quand le nombre de journees n'est pas renseigne.
function mostCommon(values) {
  const freq = new Map()
  for (const v of values) freq.set(v, (freq.get(v) ?? 0) + 1)
  return [...freq].sort((a, b) => b[1] - a[1] || b[0] - a[0])[0]?.[0] ?? 0
}

// null si tout est coherent, sinon { expected, meetings, wrongCounts, duplicates, missing }.
export function calendarAnomalies(matches, totalRounds) {
  const league = matches.filter(m => !isPlayoffMatch(m) && !isPendingTeam(m.team1Name) && !isPendingTeam(m.team2Name))
  const names = new Map()
  const counts = new Map()
  const byOrderedPair = new Map()
  for (const m of league) {
    names.set(m.team1Id, m.team1Name)
    names.set(m.team2Id, m.team2Name)
    counts.set(m.team1Id, (counts.get(m.team1Id) ?? 0) + 1)
    counts.set(m.team2Id, (counts.get(m.team2Id) ?? 0) + 1)
    const key = `${m.team1Id}-${m.team2Id}`
    if (!byOrderedPair.has(key)) byOrderedPair.set(key, [])
    byOrderedPair.get(key).push(m)
  }
  const teamIds = [...names.keys()].sort((a, b) => names.get(a).localeCompare(names.get(b)))

  // Rencontres par paire, seulement si le nombre de journees correspond a un format fixe (ex :
  // 16 equipes, 30 journees = aller-retour, chaque equipe recoit chaque adversaire 1 fois). Avec
  // un nombre impair d'equipes, une equipe est exempte a chaque journee : un tour complet dure n
  // journees au lieu de n - 1. Une equipe egaree (club mal orthographie, 1 seul match) ne compte
  // pas dans n : seules les equipes ayant joue au moins moitie moins que la plus assidue.
  const maxCount = Math.max(0, ...counts.values())
  const regulars = teamIds.filter(id => counts.get(id) >= maxCount / 2)
  const n = regulars.length
  const roundsPerCycle = n % 2 === 0 ? n - 1 : n
  const meetings = totalRounds && n > 1 && totalRounds % roundsPerCycle === 0 ? totalRounds / roundsPerCycle : null
  const expected = meetings ? meetings * (n - 1) : totalRounds ?? mostCommon([...counts.values()])

  // Calendrier pas encore saisi en entier (journees a venir pas importees) : un match manquant
  // n'est pas une erreur, seuls les matchs en trop sont signales.
  const complete = !totalRounds || league.length >= (n * expected) / 2
  const wrongCounts = teamIds
    .filter(id => (complete ? counts.get(id) !== expected : counts.get(id) > expected))
    .map(id => ({ teamId: id, name: names.get(id), count: counts.get(id) }))

  const duplicates = []
  const missing = []
  if (meetings) {
    const maxAtHome = Math.ceil(meetings / 2)
    const minAtHome = Math.floor(meetings / 2)
    const played = (home, away) => byOrderedPair.get(`${home}-${away}`)?.length ?? 0
    for (const home of regulars) {
      for (const away of regulars) {
        if (home === away) continue
        const list = byOrderedPair.get(`${home}-${away}`) ?? []
        if (list.length > maxAtHome) {
          duplicates.push({ homeName: names.get(home), awayName: names.get(away), matches: list })
        }
        // Nombre pair de rencontres : chacune recoit l'autre autant de fois, on sait qui recoit
        // dans le match manquant (meme si un doublon complete le total). Impair : on ne le sait
        // pas, signale une fois par paire.
        if (!complete) continue
        if (meetings % 2 === 0 && played(home, away) < minAtHome) {
          missing.push({ homeName: names.get(home), awayName: names.get(away), homeKnown: true })
        } else if (meetings % 2 === 1 && home < away && played(home, away) + played(away, home) < meetings) {
          missing.push({ homeName: names.get(home), awayName: names.get(away), homeKnown: false })
        }
      }
    }
  }

  if (!wrongCounts.length && !duplicates.length && !missing.length) return null
  return { expected, meetings, wrongCounts, duplicates, missing }
}
