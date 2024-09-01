<script setup lang="ts">
import { useNotificationStore } from '../stores/Notification'
import { computed } from 'vue';

const notificationStore = useNotificationStore();
const notificacao = computed(() => notificationStore.notification);

</script>

<template>
    <div class="text-center">
        <v-snackbar v-model="notificacao.visibilidade" multi-line location="top right" timeout="5000"
            :color="notificacao.tipoMensagem">
            {{ notificacao.mensagem }}

            <template v-slot:actions>
                <v-btn @click="notificacao.visibilidade = false" icon="mdi-close-circle">
                </v-btn>
            </template>
        </v-snackbar>
    </div>
</template>
