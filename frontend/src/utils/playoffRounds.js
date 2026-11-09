import { summarizeTie } from './cupTies.js'

// Barrages d'un championnat (promotion/relegation, place europeenne...) : saisis dans la
// competition du championnat avec un round_label contenant "Barrage", hors classement.
const PLAYOFF_FRAGMENT = 'BARRAGE'

export function isPlayoffMatch(match) {
  return (match.roundLabel ?? '').toUpperCase().includes(PLAYOFF_FRAGMENT)
}

function isPendingTeam(name) {
  return (name ?? '').toUpperCase().startsWith('A DETERMINER')
}

// "Barrage (a determiner : 2E-5E)" -> places "2E" et "5E" ; null hors place a determiner.
const PENDING_SLOTS_RE = /\(a d[ée]terminer\s*:([^)]*)\)/i

// Libelle du barrage sans places a determiner ni manche : "Barrages - Aller" -> "Barrages".
export function playoffLabel(raw = '') {
  const label = raw.replace(PENDING_SLOTS_RE, '').trim()
  const leg = label.match(/\b(?:aller|retour)$/i)
  return trimSeparators(leg ? label.slice(0, leg.index) : label)
}

// Retire espaces et tirets en fin de libelle (sans regex, pour eviter tout retour arriere).
function trimSeparators(text) {
  let end = text.length
  while (end > 0 && (text[end - 1] === '-' || text[end - 1].trim() === '')) end--
  return text.slice(0, end)
}

// Les 2 manches d'une confrontation : memes 2 equipes, ou, tant que les equipes restent
// "A DETERMINER", memes 2 places dans l'ordre inverse ("2E-5E" a l'aller, "5E-2E" au retour).
function tieKey(match) {
  const label = playoffLabel(match.roundLabel).toUpperCase()
  if (!isPendingTeam(match.team1Name) && !isPendingTeam(match.team2Name)) {
    return `teams-${[match.team1Id, match.team2Id].sort((a, b) => a - b).join('-')}`
  }
  const slots = (match.roundLabel ?? '').match(PENDING_SLOTS_RE)
  if (!slots) return `match-${match.id}`
  return `pending-${label}-${slots[1].split('-').map(s => s.trim().toUpperCase()).sort().join('-')}`
}

function firstDate(tie) {
  return tie.legs[0].date ?? ''
}

// Ordre d'affichage des tours : 1 seul tour = libelle du barrage, sinon libelle commun aux
// confrontations du tour quand il est plus precis que "Barrage(s)", "Ne tour" a defaut.
function roundLabel(ties, index, total) {
  const labels = [...new Set(ties.map(t => playoffLabel(t.legs[0].roundLabel)))]
  const common = labels.length === 1 ? labels[0] : null
  if (total === 1) return common || 'Barrage'
  if (common && !/^barrages?$/i.test(common)) return common
  return index === 0 ? '1er tour' : `${index + 1}e tour`
}

// Tours d'un barrage deduits de la progression des equipes, sans dependre du libelle saisi :
// une confrontation se joue au tour suivant le dernier tour d'une de ses 2 equipes (ex :
// Irlande, demi-finales 2e-5e et 3e-4e de D2, finale entre les vainqueurs, puis barrage contre
// le 9e de D1). Une confrontation encore "a determiner" se place apres les confrontations deja
// programmees avant elle sous le meme libelle. Un barrage sec donne un seul tour.
export function playoffRounds(matches) {
  const legsByTie = new Map()
  for (const m of matches.toSorted((a, b) => a.id - b.id)) {
    const key = tieKey(m)
    if (!legsByTie.has(key)) legsByTie.set(key, [])
    legsByTie.get(key).push(m)
  }
  const ties = [...legsByTie.values()]
    .map(summarizeTie)
    .sort((a, b) => firstDate(a).localeCompare(firstDate(b)) || a.legs[0].id - b.legs[0].id)

  const lastRoundOfTeam = new Map()
  const placed = []
  for (const tie of ties) {
    const label = playoffLabel(tie.legs[0].roundLabel).toUpperCase()
    const known = [[tie.teamAId, tie.teamAName], [tie.teamBId, tie.teamBName]]
      .filter(([, name]) => !isPendingTeam(name))
      .map(([id]) => id)
    let round = Math.max(-1, ...known.map(id => lastRoundOfTeam.get(id) ?? -1)) + 1
    if (known.length < 2) {
      const earlier = placed.filter(p => p.label === label && firstDate(p.tie) < firstDate(tie))
      round = Math.max(round, ...earlier.map(p => p.round + 1))
    }
    for (const id of known) lastRoundOfTeam.set(id, round)
    placed.push({ tie, round, label })
  }

  // Tours contigus : chaque tour a au moins une confrontation (tour suivant = dernier tour + 1).
  const total = placed.length ? Math.max(...placed.map(p => p.round)) + 1 : 0
  return Array.from({ length: total }, (_, index) => {
    const roundTies = placed.filter(p => p.round === index).map(p => p.tie)
    return { round: `BARRAGE-${index}`, label: roundLabel(roundTies, index, total), ties: roundTies }
  })
}
