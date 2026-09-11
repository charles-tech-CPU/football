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
      <tr v-for="(row, index) in rows" :key="row.teamId">
        <td>{{ index + 1 }}</td>
        <td>{{ row.teamName }}</td>
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
  <p v-else>Aucun match joué pour l'instant dans cette compétition.</p>
</template>

<script setup>
defineProps({
  rows: { type: Array, default: () => [] }
})
</script>
