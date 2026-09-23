<template>
  <section class="hero">
    <span class="hero-eyebrow">Clubs</span>
    <h1>Reportés / Suspendus</h1>
    <p class="section-intro">Tous les matchs reportés ou suspendus, toutes compétitions confondues.</p>
  </section>

  <div v-if="matches.length" class="table-scroll">
  <table>
    <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Compétition</th>
        <th>Journée</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in matches" :key="m.id" :class="rowClass(m)">
        <MatchDateTimeCells v-model:date="edits[m.id].date" v-model:time="edits[m.id].time" />
        <td>
          <span class="team-cell" :class="competitionBadgeClass(m)">
            <FlagIcon v-if="m.competitionCountry" :country="m.competitionCountry" />
            {{ m.competitionName }}
          </span>
        </td>
        <td>{{ m.roundLabel }}</td>
        <td>
          <select v-model.number="edits[m.id].team1Id" aria-label="Équipe domicile">
            <option v-for="t in teamOptionsFor(m, edits[m.id].team1Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </td>
        <MatchScoreCells v-model:score1="edits[m.id].score1" v-model:score2="edits[m.id].score2" />
        <td>
          <select v-model.number="edits[m.id].team2Id" aria-label="Équipe extérieur">
            <option v-for="t in teamOptionsFor(m, edits[m.id].team2Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </td>
        <td>
          <MatchStatusSelect v-model="edits[m.id].status" />
        </td>
        <td>
          <button @click="saveMatch(m)">Enregistrer</button>
        </td>
      </tr>
    </tbody>
  </table>
  </div>
  <p v-else-if="loaded" class="empty-state">Aucun match reporté ou suspendu pour l'instant.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import MatchDateTimeCells from '../components/MatchDateTimeCells.vue'
import MatchScoreCells from '../components/MatchScoreCells.vue'
import MatchStatusSelect from '../components/MatchStatusSelect.vue'
import { competitionBadgeClass } from '../utils/competitionBadge'
import { teamOptionsFor as teamOptionsAmong } from '../utils/matchEdit'
import { useMatchEdits } from '../composables/useMatchEdits'

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const { edits, error, resetEdits, rowClass, saveMatch } = useMatchEdits({ teams, reload: load })

// Clubs du pays de la competition du match (tous pour une coupe d'Europe), cf. utils/matchEdit.
function teamOptionsFor(m, currentId) {
  return teamOptionsAmong(teams.value, m.competitionCountry, currentId)
}

async function load() {
  loaded.value = false
  const [matchList, teamList] = await Promise.all([
    api.getPostponedMatches(),
    api.getTeams()
  ])
  matches.value = matchList
  teams.value = teamList
  resetEdits(matches.value)
  loaded.value = true
}

onMounted(load)
</script>
