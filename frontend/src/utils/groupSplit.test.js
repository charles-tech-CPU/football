import { describe, expect, it } from 'vitest'
import { connectedComponents, groupAssignment, opponentsGraph, sortByAverageRank, twoPoolSplit } from './groupSplit.js'

const match = (team1Id, team2Id) => ({ team1Id, team2Id })

// Mini-championnat entre toutes les equipes de la liste (chacune rencontre toutes les autres).
function roundRobin(teamIds) {
  return teamIds.flatMap((a, i) => teamIds.slice(i + 1).map(b => match(a, b)))
}

describe('opponentsGraph', () => {
  it('relie les 2 equipes de chaque match dans les 2 sens', () => {
    const graph = opponentsGraph([match(1, 2), match(2, 3)])

    expect([...graph.get(1)]).toEqual([2])
    expect([...graph.get(2)]).toEqual([1, 3])
    expect([...graph.get(3)]).toEqual([2])
  })
})

describe('connectedComponents', () => {
  it('regroupe les equipes reliees, y compris indirectement', () => {
    const graph = opponentsGraph([match(1, 2), match(3, 4), match(2, 5)])

    const components = connectedComponents(graph).map(c => [...c].sort((a, b) => a - b))

    expect(components).toEqual([[1, 2, 5], [3, 4]])
  })

  it('renvoie une liste vide pour un graphe vide', () => {
    expect(connectedComponents(new Map())).toEqual([])
  })
})

describe('sortByAverageRank', () => {
  it('place en premier le groupe dont les equipes sont les mieux classees en moyenne', () => {
    const sorted = sortByAverageRank([[3, 4], [1, 2]], [1, 2, 3, 4])

    expect(sorted).toEqual([[1, 2], [3, 4]])
  })

  it('compte une equipe absente du classement comme 1re', () => {
    const sorted = sortByAverageRank([[1, 2], [99, 4]], [1, 2, 3, 4])

    // [1, 2] : rang moyen 0.5 ; [99, 4] : (0 + 3) / 2 = 1.5
    expect(sorted).toEqual([[1, 2], [99, 4]])
  })

  it('ne modifie pas le tableau recu', () => {
    const components = [[3], [1]]

    sortByAverageRank(components, [1, 3])

    expect(components).toEqual([[3], [1]])
  })
})

describe('groupAssignment', () => {
  const ranking = [1, 2, 3, 4, 5, 6]

  it('affecte chaque equipe au groupe deduit des matchs de 2e phase, groupe du haut en 0', () => {
    // Le decoupage reel (5 en haut, 3 en bas) contredit le classement de phase 1
    const matches = [...roundRobin([1, 2, 5]), ...roundRobin([3, 4, 6])]

    const groups = groupAssignment(matches, ranking, [3, 3])

    expect(Object.fromEntries(groups)).toEqual({ 1: 0, 2: 0, 5: 0, 3: 1, 4: 1, 6: 1 })
  })

  it('accepte des groupes de tailles differentes dans le desordre', () => {
    const matches = [...roundRobin([5, 6]), ...roundRobin([1, 2, 3, 4])]

    const groups = groupAssignment(matches, ranking, [4, 2])

    expect(Object.fromEntries(groups)).toEqual({ 1: 0, 2: 0, 3: 0, 4: 0, 5: 1, 6: 1 })
  })

  it("renvoie null tant que toutes les equipes n'ont pas joue en 2e phase", () => {
    const matches = [...roundRobin([1, 2, 3]), match(4, 5)]

    expect(groupAssignment(matches, ranking, [3, 3])).toBeNull()
  })

  it('renvoie null si les groupes reconstitues ne correspondent pas aux tailles attendues', () => {
    const matches = [...roundRobin([1, 2]), ...roundRobin([3, 4, 5, 6])]

    expect(groupAssignment(matches, ranking, [3, 3])).toBeNull()
  })
})

describe('twoPoolSplit', () => {
  const ranking = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]

  it('deduit les 2 poules de 6 des matchs de poule deja joues', () => {
    const top = [1, 2, 3, 4, 5, 12]
    const bottom = [6, 7, 8, 9, 10, 11]
    const matches = [...roundRobin(bottom), ...roundRobin(top)]

    const split = twoPoolSplit(ranking, matches)

    expect([...split.top].sort((a, b) => a - b)).toEqual(top)
    expect([...split.bottom].sort((a, b) => a - b)).toEqual(bottom)
  })

  it('se rabat sur le classement de saison reguliere tant que les poules ne sont pas completes', () => {
    const split = twoPoolSplit(ranking, [match(1, 2)])

    expect(split).toEqual({ top: [1, 2, 3, 4, 5, 6], bottom: [7, 8, 9, 10, 11, 12] })
  })

  it('se rabat aussi si les composantes ne font pas 2 poules de 6', () => {
    const matches = [...roundRobin([1, 2, 3, 4]), ...roundRobin([5, 6, 7, 8, 9, 10, 11, 12])]

    expect(twoPoolSplit(ranking, matches).top).toEqual([1, 2, 3, 4, 5, 6])
  })

  it('renvoie 2 poules vides sans aucune equipe classee', () => {
    expect(twoPoolSplit([], [])).toEqual({ top: [], bottom: [] })
  })
})
