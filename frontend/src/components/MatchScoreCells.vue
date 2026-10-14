<template>
  <td>
    <input v-model.number="score1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" @keydown.enter.prevent="focusNextScore" />
  </td>
  <td>
    <input v-model.number="score2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" @keydown.enter.prevent="focusNextScore" />
  </td>
</template>

<script setup>
// Les 2 cellules de score d'une ligne de match editable.
const score1 = defineModel('score1', { type: [Number, String], default: null })
const score2 = defineModel('score2', { type: [Number, String], default: null })

// Saisie rapide : Entree passe a la case de score suivante du tableau (score 2, puis score 1
// du match suivant), sans passer par les dates et les listes d'equipes comme Tab.
function focusNextScore(event) {
  const table = event.target.closest('table')
  if (!table) return
  const inputs = [...table.querySelectorAll('.score-input')]
  const next = inputs[inputs.indexOf(event.target) + 1]
  if (next) {
    next.focus()
    next.select()
  }
}
</script>
