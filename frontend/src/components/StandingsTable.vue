<template>
  <template v-if="groupedRows.length">
    <div v-for="(grp, gi) in groupedRows" :key="grp.name ?? '_'" class="standings-group">
      <h2 v-if="grp.name">{{ grp.name }}</h2>
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Équipe</th>
            <th>MJ</th>
            <th>V</th>
            <th>N</th>
            <th>D</th>
            <th>BP</th>
            <th>BC</th>
            <th>DIFF</th>
            <th>PTS</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in grp.rows" :key="row.teamId" :class="rowClass(row, index, gi === 0)">
            <td>{{ index + 1 }}</td>
            <td>
              <span class="team-cell">
                <TeamLogo :name="row.teamName" :logo-path="row.teamLogoPath" />
                <FlagIcon v-if="showFlags" :country="row.teamCountry" />
                {{ row.teamName }}
              </span>
            </td>
            <td>{{ row.played }}</td>
            <td>{{ row.won }}</td>
            <td>{{ row.drawn }}</td>
            <td>{{ row.lost }}</td>
            <td>{{ row.goalsFor }}</td>
            <td>{{ row.goalsAgainst }}</td>
            <td>{{ row.goalDifference > 0 ? '+' : '' }}{{ row.goalDifference }}</td>
            <td><strong>{{ row.points }}</strong></td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
  <p v-else class="empty-state">Aucun match joué pour l'instant dans cette compétition.</p>

  <ul class="standings-legend" v-if="rows.length && (showBaseLegend || ldcSlots > 0 || elSlots > 0 || eclSlots > 0)">
    <template v-if="showBaseLegend">
      <li><span class="legend-swatch legend-blue"></span>Champion sortant</li>
      <li><span class="legend-swatch legend-purple"></span>Vainqueur de la coupe (préc.)</li>
      <li><span class="legend-swatch legend-red"></span>Promu</li>
    </template>
    <li v-if="ldcSlots > 0"><span class="legend-swatch legend-green"></span>Qualifié Ligue des Champions</li>
    <li v-if="elSlots > 0"><span class="legend-swatch legend-orange"></span>Qualifié Europa League</li>
    <li v-if="eclSlots > 0"><span class="legend-swatch legend-teal"></span>Qualifié Conference League</li>
  </ul>
</template>

<script setup>
import { computed } from 'vue'
import TeamLogo from './TeamLogo.vue'
import FlagIcon from './FlagIcon.vue'

const props = defineProps({
  rows: { type: Array, default: () => [] },
  teamStatuses: { type: Object, default: () => ({}) },
  ldcSlots: { type: Number, default: 0 },
  elSlots: { type: Number, default: 0 },
  eclSlots: { type: Number, default: 0 },
  showBaseLegend: { type: Boolean, default: true },
  showFlags: { type: Boolean, default: false },
  // Quand fourni, remplace toute autre couleur par un decoupage en tranches de rang,
  // ex: [{ count: 8, class: 'standing-blue' }, { count: 16, class: 'standing-green' }] puis
  // 'standing-red' pour le reste. Ignore teamStatuses/ldcSlots/elSlots/eclSlots.
  rankBands: { type: Array, default: null },
  rankBandsRest: { type: String, default: 'standing-red' }
})

const groupedRows = computed(() => {
  if (!props.rows.length) return []
  if (!props.rows.some(r => r.group)) return [{ name: null, rows: props.rows }]
  const map = new Map()
  for (const r of props.rows) {
    const key = r.group ?? '—'
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(r)
  }
  return [...map.entries()].map(([name, rows]) => ({ name, rows }))
})

function rowClass(row, index, applyQualificationColors) {
  const rank = index + 1
  if (props.rankBands) {
    let threshold = 0
    for (const band of props.rankBands) {
      threshold += band.count
      if (rank <= threshold) return band.class
    }
    return props.rankBandsRest ?? 'standing-red'
  }
  const status = props.teamStatuses[row.teamId]
  if (status?.defendingChampion) return 'standing-blue'
  if (status?.previousCupWinner) return 'standing-purple'
  if (status?.promoted) return 'standing-red'
  if (!applyQualificationColors) return ''
  if (rank <= props.ldcSlots) return 'standing-green'
  if (rank <= props.ldcSlots + props.elSlots) return 'standing-orange'
  if (rank <= props.ldcSlots + props.elSlots + props.eclSlots) return 'standing-teal'
  return ''
}
</script>

<style scoped>
.standings-group h2 {
  margin: 0 0 10px;
}

.standings-group {
  margin-bottom: 8px;
}
</style>
