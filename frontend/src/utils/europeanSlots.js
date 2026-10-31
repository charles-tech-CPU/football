// Places en coupes d'Europe de la saison suivante (vue EuropeanSlotsView) : fonctions pures.

export const SLOT_COMPETITIONS = [
  { code: 'LDC', label: 'Ligue des Champions' },
  { code: 'EL', label: 'Europa League' },
  { code: 'EC', label: 'Conference League' }
]

// Tours d'entree, du plus avance au premier tour (memes numeros que EuropeanSlotService.STAGES).
export const SLOT_STAGES = [
  { stage: 1, label: 'Phase de ligue' },
  { stage: 2, label: 'Barrage' },
  { stage: 3, label: '3e tour de qualification' },
  { stage: 4, label: 'Voie principale (2e / 3e tour)' },
  { stage: 5, label: '2e tour de qualification' },
  { stage: 6, label: '1er tour de qualification' }
]

// Saison UEFA (2028) en libelle "2027-2028".
export function seasonLabel(season) {
  return `${season - 1}-${season}`
}

function normalize(text) {
  return (text ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

// Places d'une competition regroupees par tour d'entree (du plus avance au premier tour), pays par
// ordre alphabetique ; filtre sur le pays ou le club.
export function slotsByStage(slots, competitionCode, search = '') {
  const q = normalize(search.trim())
  const matching = slots.filter(s => s.competitionCode === competitionCode &&
    (!q || normalize(s.country).includes(q) || normalize(s.teamName).includes(q)))
  return SLOT_STAGES
    .map(({ stage, label }) => ({
      stage,
      label,
      slots: matching.filter(s => s.entryStage === stage).toSorted((a, b) => a.country.localeCompare(b.country))
    }))
    .filter(group => group.slots.length)
}

// Nombre de places et de places dont le club est connu, par competition.
export function fillProgress(slots, competitionCode) {
  const ofCompetition = slots.filter(s => s.competitionCode === competitionCode)
  return { total: ofCompetition.length, filled: ofCompetition.filter(s => s.teamId != null).length }
}
