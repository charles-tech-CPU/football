<template>
  <div class="table-scroll">
    <table class="team-matches">
      <thead>
        <tr>
          <th>Date</th>
          <th>Compétition</th>
          <th>Tour</th>
          <th>Domicile</th>
          <th>Score</th>
          <th>Extérieur</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in matches" :key="m.id">
          <td class="match-date">{{ m.date ? formatShortDate(m.date) : 'Date à fixer' }}</td>
          <td>
            <span :class="competitionBadgeClass(m)">{{ m.competitionName }}</span>
          </td>
          <td class="match-round">{{ m.roundLabel }}</td>
          <td :class="{ 'is-self': m.team1Id === teamId }">
            <span class="match-team match-team--home">
              <router-link :to="`/equipes/${m.team1Id}`" class="team-link">{{ m.team1Name }}</router-link>
              <TeamLogo :name="m.team1Name" :country="m.team1Country" :logo-path="m.team1LogoPath" />
            </span>
          </td>
          <td class="match-score">
            <span v-if="isPlayed(m)" class="score-chip" :class="`result-${resultFor(m)}`">
              {{ m.score1 }} – {{ m.score2 }}
            </span>
            <span v-else class="match-time">{{ formatTime(m.time) || '—' }}</span>
            <span v-if="m.penaltyScore1 != null && m.penaltyScore2 != null" class="pen">
              (tab {{ m.penaltyScore1 }}–{{ m.penaltyScore2 }})
            </span>
          </td>
          <td :class="{ 'is-self': m.team2Id === teamId }">
            <span class="match-team">
              <TeamLogo :name="m.team2Name" :country="m.team2Country" :logo-path="m.team2LogoPath" />
              <router-link :to="`/equipes/${m.team2Id}`" class="team-link">{{ m.team2Name }}</router-link>
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
// Liste compacte des matchs d'un club (fiche club) : le score est colore selon le resultat du club.
import TeamLogo from './TeamLogo.vue'
import { competitionBadgeClass } from '../utils/competitionBadge'
import { formatShortDate, formatTime } from '../utils/format'
import { isPlayed } from '../utils/matchEdit'

const props = defineProps({
  matches: { type: Array, required: true },
  teamId: { type: Number, required: true }
})

function resultFor(m) {
  const home = m.team1Id === props.teamId
  const own = home ? m.score1 : m.score2
  const other = home ? m.score2 : m.score1
  if (own > other) return 'W'
  return own === other ? 'D' : 'L'
}
</script>

<style scoped>
.team-matches td {
  white-space: nowrap;
}

.match-date,
.match-round,
.match-time {
  color: var(--text-muted);
  font-size: 0.9em;
}

.match-team {
  display: flex;
  align-items: center;
  gap: 8px;
}

.match-team--home {
  justify-content: flex-end;
}

.is-self .team-link {
  font-weight: 800;
}

.match-score {
  text-align: center;
}

.score-chip {
  display: inline-block;
  min-width: 52px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 800;
  color: #fff;
}

.result-W {
  background: #16a34a;
}

.result-D {
  background: #9ca3af;
}

.result-L {
  background: #dc2626;
}

.pen {
  display: block;
  font-size: 0.75em;
  color: var(--text-muted);
}
</style>
