// Competitions de selections rangees comme on les cherche : par competition (Coupe du monde,
// Euro...), puis par edition (annee), chaque edition regroupant sa phase finale et ses
// qualifications. Tout se deduit du code de competition (<CONFEDERATION>_<FAMILLE>_<EDITION>,
// cf. V106..V109 et l'historique V116..V123) : FIFA_WC_2026 et les UEFA_WCQ_2026,
// CAF_WCQ_2026... forment la Coupe du monde 2026.
import { CONFEDERATIONS, confederationForCompetitionCode } from './confederations.js'

const FAMILIES = [
  { key: 'WC', label: 'Coupe du monde', pattern: /^(FIFA_WC|[A-Z]+_WCQ)_(\d{4})$/ },
  { key: 'EURO', label: 'Euro', pattern: /^(UEFA_EURO|UEFA_EUROQ)_(\d{4})$/ },
  // 1959B : seconde Copa America de 1959 (Equateur, decembre).
  { key: 'COPA', label: 'Copa América', pattern: /^(CONMEBOL_COPA)_(\d{4}B?)$/ },
  { key: 'CAN', label: 'CAN', pattern: /^(CAF_AFCON|CAF_AFCONQ|CAF_CAN)_(\d{4})$/ },
  { key: 'ASIE', label: "Coupe d'Asie", pattern: /^(AFC_ASIANCUP|AFC_ASIANCUPQ)_(\d{4})$/ },
  { key: 'GOLD', label: 'Gold Cup', pattern: /^(CONCACAF_GOLDCUP|CONCACAF_GOLDCUPQ)_(\d{4})$/ },
  { key: 'UNL', label: 'Ligue des Nations UEFA', pattern: /^(UEFA_NL)_(\d{4})$/ },
  { key: 'CNL', label: 'Ligue des Nations Concacaf', pattern: /^(CONCACAF_NL)_(\d{4})$/ }
]

// Phases de qualification : WCQ/EUROQ/AFCONQ/ASIANCUPQ, et CAF_CAN_2027 (saison en cours,
// qualifications seulement). Les Ligues des Nations n'ont qu'une phase.
const QUALIFIER_PREFIXES = new Set(['UEFA_EUROQ', 'CAF_AFCONQ', 'CAF_CAN', 'AFC_ASIANCUPQ', 'CONCACAF_GOLDCUPQ'])

// "1819" (Ligue des Nations, a cheval sur deux annees) -> "2018-2019" ; "1959B" -> "1959 (bis)" ;
// "2026" -> "2026".
function editionLabel(edition, familyKey) {
  if (edition.endsWith('B')) return `${edition.slice(0, -1)} (bis)`
  if (familyKey !== 'UNL' && familyKey !== 'CNL') return edition
  return `20${edition.slice(0, 2)}-20${edition.slice(2)}`
}

// Phase d'une competition dans son edition : libelle de l'onglet et ordre (phase finale,
// qualifications par confederation, barrages intercontinentaux).
function phaseOf(prefix) {
  if (prefix === 'FIFA_WCQ') return { label: 'Barrages intercontinentaux', order: 100 }
  if (prefix.endsWith('_WCQ')) {
    const conf = confederationForCompetitionCode(prefix)
    return { label: `Qualifications ${conf?.label ?? ''}`.trim(), order: 10 + CONFEDERATIONS.indexOf(conf) }
  }
  if (QUALIFIER_PREFIXES.has(prefix)) return { label: 'Qualifications', order: 10 }
  return { label: 'Phase finale', order: 0 }
}

// [{ key, label, current, editions: [{ key, label, current, phases: [{ label, competition }] }] }]
// Competitions et editions dans l'ordre d'affichage (editions les plus recentes d'abord) ;
// current = au moins une competition de la saison en cours. Une competition au code inconnu
// est ignoree.
export function internationalFamilies(competitions, currentSeason) {
  const families = new Map()
  for (const c of competitions) {
    const family = FAMILIES.find(f => f.pattern.test(c.code ?? ''))
    if (!family) continue
    const [, prefix, edition] = c.code.match(family.pattern)
    if (!families.has(family.key)) families.set(family.key, { key: family.key, label: family.label, editions: new Map() })
    const editions = families.get(family.key).editions
    if (!editions.has(edition)) editions.set(edition, { key: edition, label: editionLabel(edition, family.key), season: 0, phases: [] })
    const ed = editions.get(edition)
    ed.season = Math.max(ed.season, c.season ?? 0)
    ed.phases.push({ ...phaseOf(prefix), competition: c })
  }
  return FAMILIES.filter(f => families.has(f.key)).map(f => {
    const editions = [...families.get(f.key).editions.values()]
      .toSorted((a, b) => b.season - a.season || b.key.localeCompare(a.key))
      .map(ed => ({
        key: ed.key,
        label: ed.label,
        current: ed.phases.some(p => p.competition.season === currentSeason),
        phases: ed.phases.toSorted((a, b) => a.order - b.order).map(({ label, competition }) => ({ label, competition }))
      }))
    return { key: f.key, label: f.label, current: editions.some(e => e.current), editions }
  })
}

// Competition, edition et phase contenant la competition d'id donne (null si absente).
export function locateCompetition(families, competitionId) {
  for (const family of families) {
    for (const edition of family.editions) {
      const phase = edition.phases.find(p => p.competition.id === competitionId)
      if (phase) return { family, edition, phase }
    }
  }
  return null
}
