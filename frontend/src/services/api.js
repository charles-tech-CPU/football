import axios from 'axios'

// Backend Spring Boot en dev (port 8081, different du projet LoL sur 8080).
const api = axios.create({
  baseURL: 'http://localhost:8081/api'
})

export default {
  getCompetitions: () => api.get('/competitions').then(r => r.data),
  createCompetition: (payload) => api.post('/competitions', payload).then(r => r.data),

  getTeams: (country) => api.get('/teams', { params: { country } }).then(r => r.data),
  createTeam: (payload) => api.post('/teams', payload).then(r => r.data),
  mergeTeam: (id, intoId) => api.post(`/teams/${id}/merge`, null, { params: { intoId } }).then(r => r.data),

  getMatchesByCompetition: (competitionId) =>
    api.get('/matches', { params: { competitionId } }).then(r => r.data),
  getRecentResults: (limit) => api.get('/matches/recent', { params: { limit } }).then(r => r.data),
  getUpcomingMatches: (limit) => api.get('/matches/upcoming', { params: { limit } }).then(r => r.data),
  createMatch: (payload) => api.post('/matches', payload).then(r => r.data),
  updateMatch: (id, payload) => api.put(`/matches/${id}`, payload).then(r => r.data),
  deleteMatch: (id) => api.delete(`/matches/${id}`),

  getStandings: (competitionId) =>
    api.get('/standings', { params: { competitionId } }).then(r => r.data),
  getHeadToHead: (competitionId) =>
    api.get('/head-to-head', { params: { competitionId } }).then(r => r.data),

  getTeamStatuses: (competitionId) =>
    api.get('/team-status', { params: { competitionId } }).then(r => r.data),
  setTeamStatus: (competitionId, teamId, payload) =>
    api.put('/team-status', payload, { params: { competitionId, teamId } }).then(r => r.data),
  updateQualificationSlots: (competitionId, payload) =>
    api.patch(`/competitions/${competitionId}/qualification-slots`, payload).then(r => r.data)
}
