// Calendrier international : 1 confederation par prefixe de code de competition
// (voir V106..V109 et l'historique V116..V123 dans backend/src/main/resources/db/migration ;
// FIFA_ = Coupe du monde et barrages intercontinentaux, onglet "Monde"). Couleur de badge
// (comp-*, voir style.css) et de ligne de classement (standing-*/legend-*, deja
// definies pour les classements de clubs) partagees pour rester coherent visuellement.
export const CONFEDERATIONS = [
  { code: 'UEFA', label: 'Europe', prefix: 'UEFA_', badgeClass: 'comp-uefa', legendClass: 'legend-blue', rowClass: 'standing-blue' },
  { code: 'CAF', label: 'Afrique', prefix: 'CAF_', badgeClass: 'comp-caf', legendClass: 'legend-green', rowClass: 'standing-green' },
  { code: 'CONCACAF', label: 'Amérique du Nord', prefix: 'CONCACAF_', badgeClass: 'comp-concacaf', legendClass: 'legend-orange', rowClass: 'standing-orange' },
  { code: 'CONMEBOL', label: 'Amérique du Sud', prefix: 'CONMEBOL_', badgeClass: 'comp-conmebol', legendClass: 'legend-purple', rowClass: 'standing-purple' },
  { code: 'AFC', label: 'Asie', prefix: 'AFC_', badgeClass: 'comp-afc', legendClass: 'legend-teal', rowClass: 'standing-teal' },
  { code: 'OFC', label: 'Océanie', prefix: 'OFC_', badgeClass: 'comp-ofc', legendClass: 'legend-yellow', rowClass: 'standing-yellow' },
  { code: 'FIFA', label: 'Monde', prefix: 'FIFA_', badgeClass: 'comp-fifa', legendClass: 'legend-red', rowClass: 'standing-red' }
]

export function confederationForCompetitionCode(code) {
  if (!code) return null
  return CONFEDERATIONS.find(c => code.startsWith(c.prefix)) ?? null
}

export function confederationBadgeClass(m) {
  const conf = confederationForCompetitionCode(m.competitionCode)
  return conf ? `comp-badge ${conf.badgeClass}` : ''
}
