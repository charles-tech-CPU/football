<template>
  <span class="team-select">
    <input
      ref="input"
      v-model="query"
      type="text"
      role="combobox"
      autocomplete="off"
      :aria-label="ariaLabel"
      :aria-expanded="open"
      :placeholder="placeholder"
      :required="required"
      @focus="openList"
      @click="openList"
      @input="onInput"
      @keydown.down.prevent="move(1)"
      @keydown.up.prevent="move(-1)"
      @keydown.enter="onEnter"
      @keydown.esc="onEsc"
      @blur="close"
    />
    <Teleport to="body">
      <ul v-if="open" class="team-select-list" role="listbox" :style="listStyle">
        <li
          v-for="(t, i) in filtered"
          :key="t.id"
          role="option"
          :aria-selected="t.id === modelValue"
          :class="{ active: i === activeIndex, selected: t.id === modelValue }"
          @mousedown.prevent="choose(t)"
          @mouseenter="activeIndex = i"
        >
          {{ t.name }}
        </li>
        <li v-if="!filtered.length" class="team-select-empty">Aucun club</li>
      </ul>
    </Teleport>
  </span>
</template>

<script setup>
// Choix d'un club avec recherche : on tape une partie du nom ("dri" -> KF DRITA), fleches
// haut/bas pour se deplacer, Entree pour valider. Remplace un <select> (v-model = id du club).
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { filterTeams } from '../utils/teamSearch'

const props = defineProps({
  modelValue: { type: [Number, String], default: null },
  teams: { type: Array, required: true },
  ariaLabel: { type: String, default: 'Équipe' },
  placeholder: { type: String, default: 'Rechercher un club' },
  required: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const input = ref(null)
const query = ref('')
const open = ref(false)
const searching = ref(false)
const activeIndex = ref(0)
const listStyle = ref({})

const selectedName = computed(() => props.teams.find(t => t.id === props.modelValue)?.name ?? '')
const filtered = computed(() => (searching.value ? filterTeams(props.teams, query.value) : props.teams))

watch(selectedName, name => {
  if (!open.value) query.value = name
}, { immediate: true })

// La liste est affichee hors du tableau (Teleport) pour ne pas etre coupee par un conteneur
// defilant ; elle se place sous le champ et se ferme quand la page defile.
function placeList() {
  const r = input.value.getBoundingClientRect()
  const below = window.innerHeight - r.bottom
  const style = { left: `${r.left}px`, minWidth: `${Math.max(r.width, 220)}px` }
  if (below < 240 && r.top > below) style.bottom = `${window.innerHeight - r.top + 2}px`
  else style.top = `${r.bottom + 2}px`
  listStyle.value = style
}

function onScroll(e) {
  if (!e.target?.closest?.('.team-select-list')) close()
}

function openList() {
  if (open.value) return
  searching.value = false
  activeIndex.value = Math.max(0, props.teams.findIndex(t => t.id === props.modelValue))
  placeList()
  open.value = true
  input.value.select()
  window.addEventListener('scroll', onScroll, true)
  window.addEventListener('resize', close)
  scrollActiveIntoView()
}

function close() {
  if (!open.value) return
  open.value = false
  query.value = selectedName.value
  window.removeEventListener('scroll', onScroll, true)
  window.removeEventListener('resize', close)
}

// Echap ferme la liste sans fermer la fenetre qui contient le champ.
function onEsc(e) {
  if (!open.value) return
  e.stopPropagation()
  close()
}

function onInput() {
  if (!open.value) openList()
  searching.value = true
  activeIndex.value = 0
}

function move(step) {
  if (!open.value) openList()
  const count = filtered.value.length
  if (!count) return
  activeIndex.value = (activeIndex.value + step + count) % count
  scrollActiveIntoView()
}

function onEnter(e) {
  if (!open.value) return
  e.preventDefault()
  const team = filtered.value[activeIndex.value]
  if (team) choose(team)
  else close()
}

function choose(team) {
  emit('update:modelValue', team.id)
  close()
}

async function scrollActiveIntoView() {
  await nextTick()
  document.querySelector('.team-select-list .active')?.scrollIntoView({ block: 'nearest' })
}

onBeforeUnmount(close)
</script>

<style scoped>
.team-select {
  display: inline-block;
}

.team-select input {
  width: 190px;
  max-width: 100%;
}
</style>

<style>
.team-select-list {
  position: fixed;
  z-index: 1100;
  max-height: 240px;
  overflow-y: auto;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  background: var(--surface);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.team-select-list li {
  padding: 6px 12px;
  cursor: pointer;
  white-space: nowrap;
}

.team-select-list li.active {
  background: rgba(22, 135, 77, 0.12);
}

.team-select-list li.selected {
  font-weight: 700;
}

.team-select-list .team-select-empty {
  color: var(--text-muted);
  cursor: default;
}
</style>
