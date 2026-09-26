import { describe, expect, it } from 'vitest'
import { calendarProgress, isLate, isScheduled, lateCount, leagueRound } from './competitionProgress.js'

const TODAY = '2026-09-26'

describe('isLate', () => {
  it('match non joue dont la date est passee', () => {
    expect(isLate({ date: '2026-09-20' }, TODAY)).toBe(true)
  })

  it('match reporte ou suspendu, meme sans date ou date future', () => {
    expect(isLate({ date: null, status: 'POSTPONED' }, TODAY)).toBe(true)
    expect(isLate({ date: '2026-10-10', status: 'SUSPENDED' }, TODAY)).toBe(true)
  })

  it('pas en retard : joue, forfait, du jour, a venir ou sans date', () => {
    expect(isLate({ date: '2026-09-20', score1: 1, score2: 0 }, TODAY)).toBe(false)
    expect(isLate({ date: '2026-09-20', status: 'FORFEIT' }, TODAY)).toBe(false)
    expect(isLate({ date: TODAY }, TODAY)).toBe(false)
    expect(isLate({ date: '2026-10-01' }, TODAY)).toBe(false)
    expect(isLate({ date: null }, TODAY)).toBe(false)
  })
})

describe('lateCount', () => {
  it('compte les matchs en retard', () => {
    expect(lateCount([
      { date: '2026-09-01' },
      { date: '2026-09-01', score1: 2, score2: 2 },
      { date: null, status: 'POSTPONED' },
      { date: '2026-12-01' }
    ], TODAY)).toBe(2)
  })
})

describe('leagueRound', () => {
  it('numero des journees "J12", null pour les autres tours', () => {
    expect(leagueRound({ roundLabel: 'J12' })).toBe(12)
    expect(leagueRound({ roundLabel: 'Barrage (a determiner : 3E-6E)' })).toBeNull()
    expect(leagueRound({ roundLabel: null })).toBeNull()
  })
})

describe('isScheduled', () => {
  it('date et horaire requis pour un match a venir', () => {
    expect(isScheduled({ date: '2026-10-01', time: '18:00:00' })).toBe(true)
    expect(isScheduled({ date: '2026-10-01', time: null })).toBe(false)
    expect(isScheduled({ date: null, time: null })).toBe(false)
  })

  it('joue, forfait, reporte ou suspendu : considere comme programme', () => {
    expect(isScheduled({ date: '2026-09-01', time: null, score1: 1, score2: 1 })).toBe(true)
    expect(isScheduled({ date: null, time: null, status: 'FORFEIT' })).toBe(true)
    expect(isScheduled({ date: null, time: null, status: 'POSTPONED' })).toBe(true)
  })
})

describe('calendarProgress', () => {
  const m = (round, date, time) => ({ roundLabel: `J${round}`, date, time })

  it('toutes les journees programmees', () => {
    expect(calendarProgress([m(1, '2026-08-10', '18:00:00'), m(2, '2026-08-17', '20:00:00')], 2))
      .toEqual({ total: 2, complete: 2, firstMissing: null, firstUntimedDate: null })
  })

  it('journees attendues mais pas encore saisies', () => {
    expect(calendarProgress([m(1, '2026-08-10', '18:00:00'), m(2, '2026-08-17', '20:00:00')], 36))
      .toEqual({ total: 36, complete: 2, firstMissing: 3, firstUntimedDate: null })
  })

  it('horaire manquant : journee incomplete, meme si une journee suivante est complete', () => {
    expect(calendarProgress([
      m(1, '2026-08-10', '18:00:00'),
      m(2, '2026-08-17', '20:00:00'),
      m(2, '2026-08-17', null),
      m(3, '2026-08-24', '18:00:00')
    ], 3)).toEqual({ total: 3, complete: 2, firstMissing: 2, firstUntimedDate: '2026-08-17' })
  })

  it('date la plus proche d\'un match sans horaire, hors matchs joues ou sans date', () => {
    expect(calendarProgress([
      m(3, '2026-10-20', null),
      m(2, '2026-10-05', null),
      m(1, null, null),
      { roundLabel: 'J1', date: '2026-09-01', time: null, score1: 1, score2: 0 }
    ], 3).firstUntimedDate).toBe('2026-10-05')
  })

  it('sans nombre de journees attendu, se base sur la derniere journee saisie', () => {
    expect(calendarProgress([m(1, null, null), m(4, '2026-09-01', '18:00:00')]))
      .toEqual({ total: 4, complete: 1, firstMissing: 1, firstUntimedDate: null })
  })

  it('ignore les barrages et autres tours', () => {
    expect(calendarProgress([{ roundLabel: 'Barrage (a determiner : 3E-6E)', date: null }]))
      .toEqual({ total: 0, complete: 0, firstMissing: null, firstUntimedDate: null })
  })
})
