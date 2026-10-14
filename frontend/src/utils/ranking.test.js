import { describe, expect, it } from 'vitest'
import { formatRank, gapToPrevious, rankMove } from './ranking.js'

describe('rankMove', () => {
  it('places gagnees, perdues ou inchangees depuis le debut de saison', () => {
    expect(rankMove(3, 5)).toMatchObject({ label: '▲ 2', cls: 'move-up' })
    expect(rankMove(6, 5)).toMatchObject({ label: '▼ 1', cls: 'move-down' })
    expect(rankMove(5, 5)).toMatchObject({ label: '=', cls: 'move-same' })
  })

  it('rien sans rang de depart', () => {
    expect(rankMove(5, null)).toBeNull()
  })
})

describe('gapToPrevious', () => {
  const rows = [{ total: 90 }, { total: 85.5 }, { total: 85.5 }]

  it('ecart avec la ligne du dessus, rien pour la premiere', () => {
    expect(gapToPrevious(rows, 0)).toBeNull()
    expect(gapToPrevious(rows, 1)).toBe(4.5)
    expect(gapToPrevious(rows, 2)).toBe(0)
  })
})

describe('formatRank', () => {
  it('1er puis 2e, 3e...', () => {
    expect(formatRank(1)).toBe('1er')
    expect(formatRank(12)).toBe('12e')
  })
})
