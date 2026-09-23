<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-overlay" @click.self="close" @keydown.esc="close">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ title }}</h3>
          <button type="button" class="modal-close" @click="close">✕</button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal-box {
  background: var(--surface);
  border-radius: var(--radius, 10px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
  max-width: 640px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.modal-header h3 {
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.1em;
  cursor: pointer;
  color: var(--text-muted);
  line-height: 1;
  padding: 4px;
}

.modal-close:hover {
  color: var(--text);
}

.modal-body {
  padding: 20px;
}
</style>
