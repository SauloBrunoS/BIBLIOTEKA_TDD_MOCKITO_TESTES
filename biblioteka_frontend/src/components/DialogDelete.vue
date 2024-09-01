<script setup lang="ts">

import { computed } from "vue";

const emit = defineEmits(["submitted", "canceled", "update:dialogVisible"]);

const props = defineProps({
  dialogVisible: {
    type: Boolean,
  },
  descricao: {
    type: String,
  },
});

const dialog = computed({
  get() {
    return props.dialogVisible
  },
  set(newValue) {
    emit('update:dialogVisible', newValue)
  }
});

</script>

<template>
  <v-dialog v-model="dialog" persistent max-width="370">
    <v-card>
      <v-card-title class="headline"></v-card-title>
      <v-card-text>{{ props.descricao }}</v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="secundary" variant="tonal" @click="emit('canceled')">Cancelar</v-btn>
        <v-btn color="error" variant="flat" @click="emit('submitted')">Excluir</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
