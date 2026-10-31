<template>
  <details class="add-form next-season">
    <summary>🆕 Préparer la saison {{ nextSeason }}</summary>
    <p class="section-intro">
      Crée le championnat {{ nextSeason }} avec les mêmes réglages (places européennes, relégation,
      journées) et y inscrit les clubs engagés : tenant du titre, vainqueur de la coupe et promus sont
      marqués automatiquement. Le calendrier se saisit ensuite comme d'habitude.
    </p>

    <h3>Clubs de la saison {{ league.season }}</h3>
    <p class="section-intro">Décoche les clubs qui descendent (barragistes selon le résultat du barrage).</p>
    <ul class="next-season-list">
      <li v-for="(r, i) in finalStandings" :key="r.teamId" :class="`zone-${zones[i]}`">
        <label>
          <input v-model="kept" type="checkbox" :value="r.teamId" />
          <span class="next-season-rank">{{ i + 1 }}</span>
          <TeamLogo :name="r.teamName" :logo-path="r.teamLogoPath" :country="country" />
          {{ r.teamName }}
        </label>
        <span v-if="zones[i] === 'relegated'" class="next-season-tag">⬇️ relégué</span>
        <span v-else-if="zones[i] === 'barrage'" class="next-season-tag">⚔️ barrage</span>
      </li>
    </ul>

    <h3>Promus</h3>
    <ul v-if="promoted.length" class="next-season-list">
      <li v-for="t in promoted" :key="t.id">
        <TeamLogo :name="t.name" :logo-path="t.logoPath" :country="country" />
        <span class="next-season-name">{{ t.name }}</span>
        <LogoUpload :team-id="t.id" :label="t.logoPath ? '🖼️ Changer le logo' : '🖼️ Ajouter le logo'" @uploaded="onLogo" />
        <button type="button" class="delete-btn" :aria-label="`Retirer ${t.name}`" @click="removePromoted(t.id)">✕</button>
      </li>
    </ul>
    <div class="inline">
      <TeamSelect
        v-model="pick"
        :teams="candidates"
        aria-label="Club promu"
        placeholder="Club promu (taper un nom pour le créer)"
        creatable
        @create="createTeam"
      />
      <button type="button" :disabled="pick == null" @click="addPromoted">Ajouter</button>
    </div>

    <label v-if="hasCup" class="next-season-cup">
      <input v-model="withCup" type="checkbox" />
      Créer aussi la coupe nationale {{ nextSeason }} (vide, le tableau se remplit à la main)
    </label>

    <p class="section-intro">{{ kept.length + promoted.length }} clubs engagés ({{ promoted.length }} promus).</p>
    <button type="button" :disabled="busy" @click="create">Créer la saison {{ nextSeason }}</button>
    <p v-if="error" class="error-text">{{ error }}</p>
  </details>
</template>

<script setup>
// Saison suivante d'un championnat cloture (cf. NextSeasonService) : clubs maintenus, promus (crees
// au besoin, avec leur logo) et coupe nationale.
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import TeamSelect from './TeamSelect.vue'
import LogoUpload from './LogoUpload.vue'
import { relegationZone } from '../utils/seasons.js'

const props = defineProps({
  league: { type: Object, required: true },
  // Classement fige a la cloture (cf. ClosureDto.finalStandings).
  finalStandings: { type: Array, required: true },
  hasCup: { type: Boolean, default: false }
})
const emit = defineEmits(['created'])

const country = computed(() => props.league.country)
const nextSeason = computed(() => props.league.season + 1)
const zones = computed(() =>
  props.finalStandings.map((_, i) => relegationZone(i + 1, props.finalStandings.length, props.league)))
const kept = ref(props.finalStandings.filter((_, i) => zones.value[i] !== 'relegated').map(r => r.teamId))
const promoted = ref([])
const countryTeams = ref([])
const pick = ref(null)
const withCup = ref(props.hasCup)
const busy = ref(false)
const error = ref('')

// Clubs du pays pas encore engages.
const candidates = computed(() => {
  const engaged = new Set([...props.finalStandings.map(r => r.teamId), ...promoted.value.map(t => t.id)])
  return countryTeams.value.filter(t => !engaged.has(t.id))
})

function addPromoted() {
  const team = countryTeams.value.find(t => t.id === pick.value)
  if (team) promoted.value = [...promoted.value, team]
  pick.value = null
}

function removePromoted(id) {
  promoted.value = promoted.value.filter(t => t.id !== id)
}

function onLogo(team) {
  promoted.value = promoted.value.map(t => (t.id === team.id ? team : t))
}

async function createTeam(name) {
  error.value = ''
  try {
    const team = await api.createTeam({ name: name.toUpperCase(), country: country.value })
    countryTeams.value = [...countryTeams.value, team]
    promoted.value = [...promoted.value, team]
    pick.value = null
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors de la création du club.'
  }
}

async function create() {
  busy.value = true
  error.value = ''
  try {
    const created = await api.createNextSeason(props.league.id, {
      teamIds: [...kept.value, ...promoted.value.map(t => t.id)],
      promotedIds: promoted.value.map(t => t.id),
      withCup: withCup.value
    })
    emit('created', created)
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors de la création de la saison.'
  } finally {
    busy.value = false
  }
}

onMounted(async () => {
  countryTeams.value = await api.getTeams({ country: country.value })
})
</script>

<style scoped>
.next-season-list {
  max-width: 560px;
  padding: 0;
  list-style: none;
}
.next-season-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0;
}
.next-season-list label {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 8px;
}
.next-season-rank {
  min-width: 22px;
  text-align: right;
  font-weight: 700;
}
.next-season-name {
  flex: 1;
}
.next-season-tag {
  color: var(--text-muted);
  font-size: 13px;
}
.zone-relegated {
  opacity: 0.7;
}
.next-season-cup {
  display: block;
  margin: 12px 0;
}
</style>
