// Recherche de clubs dans les listes de choix : insensible a la casse et aux accents, le texte
// tape peut se trouver n'importe ou dans le nom ("dri" trouve "KF DRITA"). Les noms qui
// commencent par la recherche passent devant, puis ceux dont un mot commence par la recherche.
export function normalizeName(text) {
  return (text ?? '').normalize('NFD').replaceAll(/[̀-ͯ]/g, '').toLowerCase().trim()
}

function matchRank(name, query) {
  if (name.startsWith(query)) return 0
  if (name.split(/[\s.\-'/]+/).some(word => word.startsWith(query))) return 1
  return name.includes(query) ? 2 : -1
}

export function filterTeams(teams, query) {
  const q = normalizeName(query)
  if (!q) return teams
  return teams
    .map(team => ({ team, rank: matchRank(normalizeName(team.name), q) }))
    .filter(r => r.rank >= 0)
    .sort((a, b) => a.rank - b.rank)
    .map(r => r.team)
}
