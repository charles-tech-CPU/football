import { describe, expect, it } from 'vitest'
import { filterTeams, normalizeName } from './teamSearch.js'

const teams = [
  { id: 1, name: 'ANDERLECHT' },
  { id: 2, name: 'KF DRITA' },
  { id: 3, name: 'DRITA B' },
  { id: 4, name: 'Atlético Madrid' },
  { id: 5, name: 'HADRIAN' }
]

describe('filterTeams', () => {
  it('trouve le texte tape n importe ou dans le nom', () => {
    expect(filterTeams(teams, 'Dri').map(t => t.id)).toEqual([3, 2, 4, 5])
  })

  it('ignore la casse et les accents', () => {
    expect(filterTeams(teams, 'atletico').map(t => t.id)).toEqual([4])
    expect(filterTeams(teams, 'MADRID').map(t => t.id)).toEqual([4])
  })

  it('renvoie tous les clubs sans recherche', () => {
    expect(filterTeams(teams, '  ')).toHaveLength(5)
  })

  it('renvoie une liste vide sans correspondance', () => {
    expect(filterTeams(teams, 'xyz')).toEqual([])
  })
})

describe('normalizeName', () => {
  it('retire accents et majuscules', () => {
    expect(normalizeName(' Ümraniyespor ')).toBe('umraniyespor')
    expect(normalizeName(null)).toBe('')
  })
})
