import axios from 'axios'

// Backend Spring Boot en dev (port 8081, different du projet LoL sur 8080).
const BACKEND_BASE_URL = 'http://localhost:8081'

const api = axios.create({
  baseURL: `${BACKEND_BASE_URL}/api`
})

// Blasons des clubs : servis en statique par le backend sous /logos/<fichier>.
export function logoUrl(logoPath) {
  return logoPath ? `${BACKEND_BASE_URL}/logos/${logoPath}` : null
}

export default {
  getCompetitions: () => api.get('/competitions').then(r => r.data),
  createCompetition: (payload) => api.post('/competitions', payload).then(r => r.data),

  getTeams: (params) => api.get('/teams', { params }).then(r => r.data),
  createTeam: (payload) => api.post('/teams', payload).then(r => r.data),
  mergeTeam: (id, intoId) => api.post(`/teams/${id}/merge`, null, { params: { intoId } }).then(r => r.data),

  getMatchesByCompetition: (competitionId) =>
    api.get('/matches', { params: { competitionId } }).then(r => r.data),
  getUpcomingMatches: (params) => api.get('/matches/upcoming', { params }).then(r => r.data),
  getPostponedMatches: () => api.get('/matches/postponed').then(r => r.data),
  createMatch: (payload) => api.post('/matches', payload).then(r => r.data),
  updateMatch: (id, payload) => api.put(`/matches/${id}`, payload).then(r => r.data),
  deleteMatch: (id) => api.delete(`/matches/${id}`),

  getStandings: (competitionId, round) =>
    api.get('/standings', { params: { competitionId, round } }).then(r => r.data),
  getHeadToHead: (competitionId) =>
    api.get('/head-to-head', { params: { competitionId } }).then(r => r.data),

  getTeamStatuses: (competitionId) =>
    api.get('/team-status', { params: { competitionId } }).then(r => r.data),
  setTeamStatus: (competitionId, teamId, payload) =>
    api.put('/team-status', payload, { params: { competitionId, teamId } }).then(r => r.data),
  updateQualificationSlots: (competitionId, payload) =>
    api.patch(`/competitions/${competitionId}/qualification-slots`, payload).then(r => r.data),

  getClubUefaRankings: () => api.get('/uefa-rankings/clubs').then(r => r.data),
  getCountryUefaRankings: () => api.get('/uefa-rankings/countries').then(r => r.data)
}
