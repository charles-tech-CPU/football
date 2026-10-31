import { describe, expect, it } from 'vitest'
import { fillProgress, seasonLabel, slotsByStage } from './europeanSlots.js'

const slot = (id, competitionCode, entryStage, country, teamId = null, teamName = null) =>
  ({ id, competitionCode, entryStage, country, teamId, teamName })

const slots = [
  slot(1, 'LDC', 6, 'Norvege'),
  slot(2, 'LDC', 6, 'Finlande', 10, 'KUPS'),
  slot(3, 'LDC', 1, 'France', 20, 'PSG'),
  slot(4, 'EC', 6, 'Finlande')
]

describe('seasonLabel', () => {
  it('ecrit la saison UEFA sur deux annees', () => {
    expect(seasonLabel(2028)).toBe('2027-2028')
  })
})

describe('slotsByStage', () => {
  it('regroupe par tour, du plus avance au premier tour, pays par ordre alphabetique', () => {
    const groups = slotsByStage(slots, 'LDC')
    expect(groups.map(g => g.label)).toEqual(['Phase de ligue', '1er tour de qualification'])
    expect(groups[1].slots.map(s => s.country)).toEqual(['Finlande', 'Norvege'])
  })

  it('filtre sur le pays ou le club, sans tenir compte des accents', () => {
    expect(slotsByStage(slots, 'LDC', 'norvège').flatMap(g => g.slots).map(s => s.id)).toEqual([1])
    expect(slotsByStage(slots, 'LDC', 'kups').flatMap(g => g.slots).map(s => s.id)).toEqual([2])
  })
})

describe('fillProgress', () => {
  it('compte les places dont le club est connu', () => {
    expect(fillProgress(slots, 'LDC')).toEqual({ total: 3, filled: 2 })
  })
})
