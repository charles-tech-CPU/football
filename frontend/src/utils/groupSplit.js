// Composition reelle des groupes d'un championnat scinde, deduite des matchs de 2e phase deja
// joues : 2 equipes qui se rencontrent en 2e phase appartiennent forcement au meme groupe, donc
// chaque groupe est une composante connexe du graphe "a joue contre".

// Graphe equipe -> adversaires rencontres.
export function opponentsGraph(matches) {
  const graph = new Map()
  for (const m of matches) {
    if (!graph.has(m.team1Id)) graph.set(m.team1Id, new Set())
    if (!graph.has(m.team2Id)) graph.set(m.team2Id, new Set())
    graph.get(m.team1Id).add(m.team2Id)
    graph.get(m.team2Id).add(m.team1Id)
  }
  return graph
}

// Composantes connexes du graphe (parcours en profondeur), dans l'ordre de premiere apparition.
export function connectedComponents(graph) {
  const visited = new Set()
  const components = []
  for (const teamId of graph.keys()) {
    if (!visited.has(teamId)) components.push(explore(graph, teamId, visited))
  }
  return components
}

function explore(graph, start, visited) {
  const stack = [start]
  const component = []
  visited.add(start)
  while (stack.length) {
    const current = stack.pop()
    component.push(current)
    for (const next of graph.get(current) ?? []) {
      if (!visited.has(next)) {
        visited.add(next)
        stack.push(next)
      }
    }
  }
  return component
}

// Trie les groupes du mieux classe au moins bien classe (rang moyen de leurs equipes dans
// rankedTeamIds, une equipe absente comptant comme 1re).
export function sortByAverageRank(components, rankedTeamIds) {
  const rankOf = new Map(rankedTeamIds.map((id, i) => [id, i]))
  const averageRank = comp => comp.reduce((sum, id) => sum + (rankOf.get(id) ?? 0), 0) / comp.length
  return [...components].sort((a, b) => averageRank(a) - averageRank(b))
}

// Index de groupe (0 = groupe du haut) de chaque equipe, ou null si les matchs de 2e phase ne
// suffisent pas encore a reconstituer des groupes de la taille attendue (repli a gerer par
// l'appelant, ex: decoupage du classement de phase 1).
export function groupAssignment(phase2Matches, rankedTeamIds, sizes) {
  const graph = opponentsGraph(phase2Matches)
  if (graph.size !== rankedTeamIds.length) return null

  const components = connectedComponents(graph)
  const hasExpectedSizes = list => list.every((c, i) => c.length === sizes[i])
  const bySize = [...components].sort((a, b) => b.length - a.length)
  if (!(components.length === sizes.length && hasExpectedSizes(components)) && !hasExpectedSizes(bySize)) {
    return null
  }

  const groupOfTeam = new Map()
  sortByAverageRank(components, rankedTeamIds).forEach((comp, i) => {
    for (const id of comp) groupOfTeam.set(id, i)
  })
  return groupOfTeam
}

// Malte : 2 poules de 6 (Championnat / Maintien) deduites des matchs de poule deja joues ; a
// defaut, repli sur le classement de la saison reguliere (6 premiers en haut, 6 suivants en bas).
export function twoPoolSplit(rankedTeamIds, poolMatches) {
  const graph = opponentsGraph(poolMatches)
  if (graph.size === rankedTeamIds.length && rankedTeamIds.length > 0) {
    const components = connectedComponents(graph)
    if (components.length === 2 && components.every(c => c.length === 6)) {
      const [top, bottom] = sortByAverageRank(components, rankedTeamIds)
      return { top, bottom }
    }
  }
  return { top: rankedTeamIds.slice(0, 6), bottom: rankedTeamIds.slice(6, 12) }
}
