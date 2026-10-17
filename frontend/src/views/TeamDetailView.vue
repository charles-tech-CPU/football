<template>
  <template v-if="profile">
    <section class="hero">
      <span class="hero-eyebrow">
        <FlagIcon v-if="profile.team.country" :country="profile.team.country" />
        {{ profile.team.country ?? 'Club' }}
      </span>
      <h1 class="team-title">
        <TeamLogo :name="profile.team.name" :country="profile.team.country" :logo-path="profile.team.logoPath" />
        {{ profile.team.name }}
      </h1>
      <router-link :to="{ name: 'head-to-head', query: { equipe1: profile.team.id } }" class="h2h-link">⚔️ Tête-à-tête avec une autre équipe</router-link>

      <div class="hero-stats">
        <div v-if="profile.league" class="hero-stat">
          <span class="hero-stat-value">{{ profile.league.rank ? formatRank(profile.league.rank) : '—' }}</span>
          <span class="hero-stat-label">
            en championnat ({{ profile.league.teamCount }} clubs) · {{ profile.league.points }} pts
          </span>
        </div>
        <div v-if="profile.uefa" class="hero-stat">
          <span class="hero-stat-value">{{ formatRank(profile.uefa.rank) }}</span>
          <span class="hero-stat-label">
            au classement UEFA · {{ formatNumber(profile.uefa.total) }} pts
            <RankMove :rank="profile.uefa.rank" :start-rank="profile.uefa.startRank" />
          </span>
        </div>
        <div v-if="profile.streak" class="hero-stat">
          <span class="hero-stat-value">{{ profile.streak.length }}</span>
          <span class="hero-stat-label">{{ streakLabel(profile.streak) }}</span>
        </div>
        <div v-if="profile.form.length" class="hero-stat">
          <span class="form-chips" :aria-label="`Forme : ${profile.form.join(' ')}`">
            <span v-for="(r, i) in profile.form" :key="i" class="form-chip" :class="`form-${r}`">{{ RESULT_LETTERS[r] }}</span>
          </span>
          <span class="hero-stat-label">{{ profile.form.length }} derniers matchs (le plus récent à gauche)</span>
        </div>
      </div>
    </section>

    <p v-if="profile.league" class="section-intro">
      <router-link :to="`/competitions/${profile.league.competitionId}`">{{ profile.league.competitionName }}</router-link>
      : {{ profile.league.played }} matchs joués, {{ profile.league.goalsFor }} buts marqués,
      {{ profile.league.goalsAgainst }} encaissés.
    </p>

    <h2>Prochains matchs</h2>
    <TeamMatchList v-if="profile.upcomingMatches.length" :matches="profile.upcomingMatches" :team-id="profile.team.id" />
    <p v-else class="empty-state">Aucun match à venir.</p>

    <h2>Derniers matchs</h2>
    <TeamMatchList v-if="profile.recentMatches.length" :matches="profile.recentMatches" :team-id="profile.team.id" />
    <p v-else class="empty-state">Aucun match joué.</p>

    <template v-if="profile.uefa">
      <h2>Coefficient UEFA</h2>
      <UefaHistoryChart v-if="history.length" :points="history" :title="profile.team.name" />
    </template>
  </template>
  <p v-else-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import TeamLogo from '../components/TeamLogo.vue'
import RankMove from '../components/RankMove.vue'
import TeamMatchList from '../components/TeamMatchList.vue'
import UefaHistoryChart from '../components/UefaHistoryChart.vue'
import { formatRank } from '../utils/ranking'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const RESULT_LETTERS = { W: 'V', D: 'N', L: 'D' }

const profile = ref(null)
const history = ref([])
const error = ref('')

function formatNumber(v) {
  if (v == null) return '—'
  return Number.isInteger(v) ? String(v) : String(Number(v.toFixed(3)))
}

// Serie en cours, completee de la serie sans defaite quand elle est plus longue (ex: V V N V).
function streakLabel(streak) {
  const plural = streak.length > 1 ? 's' : ''
  const words = { W: `victoire${plural}`, D: `nul${plural}`, L: `défaite${plural}` }
  const unbeaten = streak.unbeaten > streak.length ? ` · ${streak.unbeaten} matchs sans défaite` : ''
  return `${words[streak.result]} de suite${unbeaten}`
}

async function load() {
  error.value = ''
  profile.value = null
  history.value = []
  try {
    profile.value = await api.getTeamProfile(props.id)
    if (profile.value.uefa) history.value = await api.getClubUefaHistory(profile.value.uefa.rankingId)
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Impossible de charger la fiche du club.'
  }
}

watch(() => props.id, load)
onMounted(load)
</script>

<style scoped>
.h2h-link {
  display: inline-block;
  margin-bottom: 12px;
  font-weight: 600;
}

.team-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-chips {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}

.form-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  font-weight: 800;
  color: #fff;
}

.form-W {
  background: #16a34a;
}

.form-D {
  background: #9ca3af;
}

.form-L {
  background: #dc2626;
}
</style>
