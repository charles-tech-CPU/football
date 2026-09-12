// Code couleur rapide pour reperer d'un coup d'oeil, dans un calendrier
// multi-competitions, les 5 types de matchs : championnat, coupe nationale,
// LDC, Europa League, Conference League.
export function competitionBadgeClass(m) {
  if (m.competitionCode === 'LDC') return 'comp-badge comp-ldc'
  if (m.competitionCode === 'EL') return 'comp-badge comp-el'
  if (m.competitionCode === 'EC') return 'comp-badge comp-ecl'
  if (m.competitionType === 'DOMESTIC_CUP') return 'comp-badge comp-cup'
  if (m.competitionType === 'LEAGUE') return 'comp-badge comp-league'
  return ''
}
