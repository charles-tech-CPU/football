<template>
  <h1>Classement UEFA</h1>
  <p class="section-intro">
    Coefficient UEFA des clubs et des pays. Colonnes reprises telles quelles du fichier source,
    sauf "Pts 2027" (saison en cours) : recalculee en direct a partir des matchs de phase de
    ligue LDC/EL/EC deja saisis (victoire = 2 pts, nul = 1 pt, defaite = 0 pt).
  </p>

  <div class="tab-bar">
    <button class="tab-btn" :class="{ active: activeTab === 'clubs' }" @click="activeTab = 'clubs'">Clubs</button>
    <button class="tab-btn" :class="{ active: activeTab === 'pays' }" @click="activeTab = 'pays'">Pays</button>
  </div>

  <template v-if="activeTab === 'clubs'">
    <div class="table-scroll" v-if="clubs.length">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Club</th>
            <th>Coupe</th>
            <th>Total</th>
            <th>Pts 2027</th>
            <th>2026</th>
            <th>2025</th>
            <th>2024</th>
            <th>2023</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in clubs" :key="c.id">
            <td>{{ c.rank }}</td>
            <td>
              <span class="team-cell">
                <TeamLogo :name="c.clubName" :logo-path="c.teamLogoPath" :country="c.country" />
                <FlagIcon :country="c.country" />
                {{ c.clubName }}
              </span>
            </td>
            <td><span v-if="c.currentCup" class="comp-badge" :class="cupClass(c.currentCup)">{{ c.currentCup }}</span></td>
            <td>{{ formatNumber(c.total) }}</td>
            <td><strong>{{ formatNumber(c.points2027) }}</strong></td>
            <td>{{ formatNumber(c.points2026) }}</td>
            <td>{{ formatNumber(c.points2025) }}</td>
            <td>{{ formatNumber(c.points2024) }}</td>
            <td>{{ formatNumber(c.points2023) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else-if="loaded" class="empty-state">Aucun classement disponible.</p>
  </template>

  <template v-else>
    <div class="table-scroll" v-if="countries.length">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Pays</th>
            <th>Total</th>
            <th>Pts 2027</th>
            <th>2026</th>
            <th>2025</th>
            <th>2024</th>
            <th>2023</th>
            <th>Clubs encore en Europe (LDC/EL/EC)</th>
            <th>Nb clubs 2027</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in countries" :key="c.id" :class="rowClass(c)">
            <td>{{ c.rank }}</td>
            <td>
              <span class="team-cell">
                <FlagIcon :country="c.country" />
                {{ c.country }}
              </span>
            </td>
            <td>{{ formatNumber(c.total) }}</td>
            <td><strong>{{ formatNumber(c.points2027) }}</strong></td>
            <td>{{ formatNumber(c.points2026) }}</td>
            <td>{{ formatNumber(c.points2025) }}</td>
            <td>{{ formatNumber(c.points2024) }}</td>
            <td>{{ formatNumber(c.points2023) }}</td>
            <td>{{ c.ldcNow ?? 0 }} / {{ c.elNow ?? 0 }} / {{ c.ecNow ?? 0 }}</td>
            <td>{{ c.nb2027 ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else-if="loaded" class="empty-state">Aucun classement disponible.</p>

    <ul class="standings-legend" v-if="countries.length">
      <li><span class="legend-swatch legend-blue"></span>Encore un club en Ligue des Champions</li>
      <li><span class="legend-swatch legend-orange-dark"></span>Encore un club en Europa League</li>
      <li><span class="legend-swatch legend-yellow"></span>Encore un club en Conference League</li>
      <li><span class="legend-swatch legend-red"></span>Plus aucun club en coupe d'Europe</li>
    </ul>
  </template>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/api'
import TeamLogo from '../components/TeamLogo.vue'
import FlagIcon from '../components/FlagIcon.vue'

const activeTab = ref('clubs')
const clubs = ref([])
const countries = ref([])
const loaded = ref(false)

function formatNumber(v) {
  if (v == null) return '—'
  return Number.isInteger(v) ? String(v) : v.toFixed(3).replace(/\.?0+$/, '')
}

function cupClass(cup) {
  if (cup === 'LDC') return 'comp-ldc'
  if (cup === 'EL') return 'comp-el'
  if (cup === 'EC') return 'comp-ecl'
  return ''
}

const COLOR_ROW_CLASS = { BLUE: 'standing-blue', ORANGE: 'standing-orange-dark', YELLOW: 'standing-yellow', RED: 'standing-red' }

function rowClass(c) {
  return COLOR_ROW_CLASS[c.colorCode] ?? ''
}

async function load() {
  loaded.value = false
  const [clubList, countryList] = await Promise.all([
    api.getClubUefaRankings(),
    api.getCountryUefaRankings()
  ])
  clubs.value = clubList
  countries.value = countryList
  loaded.value = true
}

onMounted(load)
</script>

<style scoped>
.table-scroll {
  overflow-x: auto;
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  padding: 0 20px;
}
</style>
