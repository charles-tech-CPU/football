<template>
  <label class="logo-upload">
    <input type="file" accept="image/png,image/jpeg,image/webp,image/gif" :disabled="busy" @change="upload" />
    <span>{{ busy ? 'Envoi...' : label }}</span>
  </label>
  <span v-if="error" class="error-text">{{ error }}</span>
</template>

<script setup>
// Envoi du blason d'un club (PNG, JPEG, WebP ou GIF, 2 Mo max), cf. LogoService.
import { ref } from 'vue'
import api from '../services/api'

const props = defineProps({
  teamId: { type: Number, required: true },
  label: { type: String, default: '🖼️ Changer le logo' }
})
const emit = defineEmits(['uploaded'])

const busy = ref(false)
const error = ref('')

async function upload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  busy.value = true
  error.value = ''
  try {
    emit('uploaded', await api.uploadTeamLogo(props.teamId, file))
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Logo refusé (PNG, JPEG, WebP ou GIF, 2 Mo maximum).'
  } finally {
    busy.value = false
    event.target.value = ''
  }
}
</script>

<style scoped>
.logo-upload {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  text-decoration: underline;
}
.logo-upload input {
  display: none;
}
</style>
