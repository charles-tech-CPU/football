<template>
  <table v-if="rows.length">
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
      <tr v-for="(row, index) in rows" :key="row.teamId" :class="rowClass(row, index)">
        <td>{{ index + 1 }}</td>
        <td><span class="team-cell"><TeamLogo :name="row.teamName" /> {{ row.teamName }}</span></td>
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
  <p v-else class="empty-state">Aucun match joué pour l'instant dans cette compétition.</p>

  <ul class="standings-legend" v-if="rows.length">
    <li><span class="legend-swatch legend-blue"></span>Champion sortant</li>
    <li><span class="legend-swatch legend-orange"></span>Vainqueur de la coupe (préc.)</li>
    <li><span class="legend-swatch legend-red"></span>Promu</li>
    <li v-if="ldcSlots > 0"><span class="legend-swatch legend-green"></span>Qualifié Ligue des Champions</li>
    <li v-if="elSlots > 0"><span class="legend-swatch legend-darkorange"></span>Qualifié Europa League</li>
    <li v-if="eclSlots > 0"><span class="legend-swatch legend-yellow"></span>Qualifié Conference League</li>
  </ul>
</template>

<script setup>
import TeamLogo from './TeamLogo.vue'

const props = defineProps({
  rows: { type: Array, default: () => [] },
  teamStatuses: { type: Object, default: () => ({}) },
  ldcSlots: { type: Number, default: 0 },
  elSlots: { type: Number, default: 0 },
  eclSlots: { type: Number, default: 0 }
})

function rowClass(row, index) {
  const status = props.teamStatuses[row.teamId]
  const rank = index + 1
  if (status?.defendingChampion) return 'standing-blue'
  if (status?.previousCupWinner) return 'standing-orange'
  if (status?.promoted) return 'standing-red'
  if (rank <= props.ldcSlots) return 'standing-green'
  if (rank <= props.ldcSlots + props.elSlots) return 'standing-darkorange'
  if (rank <= props.ldcSlots + props.elSlots + props.eclSlots) return 'standing-yellow'
  return ''
}
</script>
