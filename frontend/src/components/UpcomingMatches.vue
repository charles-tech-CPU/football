<template>
  <p class="section-intro">Matchs pas encore joués (à venir, reportés, suspendus ou forfait).</p>

  <table v-if="upcoming.length">
    <thead>
      <tr>
        <th>Round</th>
        <th>Date</th>
        <th>Heure</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in upcoming" :key="m.id" :class="rowClass(m)">
        <td>{{ m.roundLabel }}</td>
        <MatchDateTimeCells v-model:date="edits[m.id].date" v-model:time="edits[m.id].time" />
        <td>
          <span class="team-cell">
            <TeamLogo :name="m.team1Name" :logo-path="m.team1LogoPath" />
            <select v-model.number="edits[m.id].team1Id" aria-label="Équipe domicile">
              <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </span>
        </td>
        <MatchScoreCells v-model:score1="edits[m.id].score1" v-model:score2="edits[m.id].score2" />
        <td>
          <span class="team-cell">
            <TeamLogo :name="m.team2Name" :logo-path="m.team2LogoPath" />
            <select v-model.number="edits[m.id].team2Id" aria-label="Équipe extérieur">
              <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </span>
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
  <p v-else-if="loaded" class="empty-state">Tous les matchs de cette compétition ont été joués.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import MatchDateTimeCells from './MatchDateTimeCells.vue'
import MatchScoreCells from './MatchScoreCells.vue'
import MatchStatusSelect from './MatchStatusSelect.vue'
import { useMatchEdits } from '../composables/useMatchEdits'

const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  // Fragments (insensibles a la casse) : un match n'est garde que si son round_label
  // contient au moins un de ces fragments.
  roundIncludes: { type: Array, default: null },
  // Fragments (insensibles a la casse) : un match est ecarte si son round_label
  // contient l'un de ces fragments (applique apres roundIncludes).
  roundExcludes: { type: Array, default: null }
})

const matches = ref([])
const teams = ref([])
const loaded = ref(false)

const sortedTeams = computed(() => teams.value.slice().sort((a, b) => a.name.localeCompare(b.name)))

const upcoming = computed(() => {
  let list = matches.value.filter(m => m.status !== 'COMPLETED')
  if (props.roundIncludes) {
    const fragments = props.roundIncludes.map(f => f.toUpperCase())
    list = list.filter(m => fragments.some(f => (m.roundLabel ?? '').toUpperCase().includes(f)))
  }
  if (props.roundExcludes) {
    const fragments = props.roundExcludes.map(f => f.toUpperCase())
    list = list.filter(m => !fragments.some(f => (m.roundLabel ?? '').toUpperCase().includes(f)))
  }
  return list
})

const { edits, error, resetEdits, rowClass, saveMatch } = useMatchEdits({ teams, reload: load })

async function load() {
  loaded.value = false
  const competitionId = Number(props.competitionId)
  const [matchList, teamList] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getTeams({ competitionId })
  ])
  matches.value = matchList
  teams.value = teamList
  resetEdits(matches.value)
  loaded.value = true
}

watch(() => props.competitionId, load)
onMounted(load)
</script>
