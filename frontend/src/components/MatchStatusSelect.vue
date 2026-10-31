<template>
  <select v-model="choice" aria-label="Statut">
    <option value="">À venir</option>
    <option value="POSTPONED">Reporté</option>
    <option value="SUSPENDED">Suspendu</option>
    <option value="FORFEIT_1">Forfait {{ team1Name }}</option>
    <option value="FORFEIT_2">Forfait {{ team2Name }}</option>
    <option v-if="choice === 'FORFEIT'" value="FORFEIT">Forfait (équipe à préciser)</option>
  </select>
</template>

<script setup>
import { computed } from 'vue'
import { applyStatusChoice, statusChoice } from '../utils/matchEdit.js'

// Statut force d'un match pas encore joue ("" = a venir, statut deduit des scores). Choisir le
// forfait d'une equipe remplit le score sur tapis vert (3-0 pour l'adversaire, cf. matchEdit).
const status = defineModel({ type: String, default: '' })
const score1 = defineModel('score1', { type: [Number, String], default: null })
const score2 = defineModel('score2', { type: [Number, String], default: null })

defineProps({
  team1Name: { type: String, default: 'équipe 1' },
  team2Name: { type: String, default: 'équipe 2' }
})

const choice = computed({
  get: () => statusChoice({ status: status.value, score1: score1.value, score2: score2.value }),
  set: value => {
    const next = applyStatusChoice({ status: status.value, score1: score1.value, score2: score2.value }, value)
    status.value = next.status
    score1.value = next.score1
    score2.value = next.score2
  }
})
</script>
