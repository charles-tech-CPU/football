import { describe, expect, it } from 'vitest'
import { calendarAnomalies } from './calendarCheck.js'

const TEAMS = ['KAIRAT', 'KAISAR', 'ULYTAU', 'ZHETYSU'].map((name, i) => ({ id: i + 1, name }))

let nextId = 1
function match(home, away, roundLabel = 'J1') {
  return { id: nextId++, roundLabel, team1Id: home.id, team1Name: home.name, team2Id: away.id, team2Name: away.name }
}

// Aller-retour complet entre les 4 equipes : 6 journees, chacune recoit chaque adversaire 1 fois.
function doubleRoundRobin() {
  return TEAMS.flatMap(home => TEAMS.filter(away => away !== home).map(away => match(home, away)))
}

describe('calendarAnomalies', () => {
  it('calendrier complet : rien a signaler, barrages ignores', () => {
    const [kairat, kaisar] = TEAMS
    const barrage = match(kairat, kaisar, 'Barrage')

    expect(calendarAnomalies([...doubleRoundRobin(), barrage], 6)).toBeNull()
  })

  it('resultat saisi sur le mauvais match : doublon, manques et equipes au mauvais compte', () => {
    const [kairat, kaisar, ulytau, zhetysu] = TEAMS
    const matches = doubleRoundRobin()
    // KAIRAT-ULYTAU a ete transforme en ZHETYSU-KAISAR, deja joue.
    const wrong = matches.find(m => m.team1Id === kairat.id && m.team2Id === ulytau.id)
    Object.assign(wrong, { team1Id: zhetysu.id, team1Name: zhetysu.name, team2Id: kaisar.id, team2Name: kaisar.name })

    const result = calendarAnomalies(matches, 6)

    expect(result.wrongCounts).toEqual([
      { teamId: kairat.id, name: 'KAIRAT', count: 5 },
      { teamId: kaisar.id, name: 'KAISAR', count: 7 },
      { teamId: ulytau.id, name: 'ULYTAU', count: 5 },
      { teamId: zhetysu.id, name: 'ZHETYSU', count: 7 }
    ])
    expect(result.duplicates.map(d => [d.homeName, d.awayName, d.matches.length])).toEqual([['ZHETYSU', 'KAISAR', 2]])
    expect(result.missing.map(p => [p.homeName, p.awayName])).toEqual([['KAIRAT', 'ULYTAU']])
  })

  it('nombre de journees inconnu : seul le nombre de matchs par equipe est controle', () => {
    const matches = doubleRoundRobin().slice(1)

    const result = calendarAnomalies(matches, null)

    expect(result.wrongCounts.map(t => t.name)).toEqual(['KAIRAT', 'KAISAR'])
    expect(result.missing).toEqual([])
  })
})

describe('calendarAnomalies - formats particuliers', () => {
  it('club mal orthographie sur un match : signale sans fausser le format', () => {
    const [kairat, kaisar] = TEAMS
    const matches = doubleRoundRobin()
    Object.assign(matches.find(m => m.team1Id === kairat.id && m.team2Id === kaisar.id), { team2Id: 99, team2Name: 'KAISSAR' })

    const result = calendarAnomalies(matches, 6)

    expect(result.wrongCounts.map(t => [t.name, t.count])).toEqual([['KAISAR', 5], ['KAISSAR', 1]])
    expect(result.missing.map(p => [p.homeName, p.awayName])).toEqual([['KAIRAT', 'KAISAR']])
  })

  it("nombre impair d'equipes : une exempte par journee, 2 matchs par equipe sur 3 journees", () => {
    const [a, b, c] = TEAMS
    expect(calendarAnomalies([match(a, b), match(b, c), match(c, a)], 3)).toBeNull()
  })

  it('calendrier pas encore saisi en entier : seuls les doublons sont signales', () => {
    const [kairat, kaisar] = TEAMS
    const firstLeg = doubleRoundRobin().filter(m => m.team1Id < m.team2Id)
    const result = calendarAnomalies([...firstLeg, match(kairat, kaisar)], 6)

    expect(result.wrongCounts).toEqual([])
    expect(result.missing).toEqual([])
    expect(result.duplicates).toHaveLength(1)
  })
})
