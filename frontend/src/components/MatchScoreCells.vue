<template>
  <td>
    <input v-model.number="score1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" />
  </td>
  <td>
    <input v-model.number="score2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" />
  </td>
</template>

<script setup>
// Les 2 cellules de score d'une ligne de match editable.
const score1 = defineModel('score1', { type: [Number, String], default: null })
const score2 = defineModel('score2', { type: [Number, String], default: null })
</script>
