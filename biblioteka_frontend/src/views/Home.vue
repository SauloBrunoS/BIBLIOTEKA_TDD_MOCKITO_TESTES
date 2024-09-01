<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import Colecao from './colecao/Colecao.vue';
import Autor from './autor/Autor.vue';
import Livro from './livro/Livro.vue';
import Leitor from './leitor/Leitor.vue';

const selectedTab = ref('colecoes');

const tabs = [
    { id: 'colecoes', label: 'Coleções', value: 'colecoes' },
    { id: 'autores', label: 'Autores', value: 'autores' },
    { id: 'livros', label: 'Livros', value: 'livros' },
    { id: 'leitores', label: 'Leitores', value: 'leitores' }
];

onMounted(() => {
    selectedTab.value = 'colecoes';
})

</script>

<template>
    <v-card class="mt-8" elevation="0" color="surface">
        <v-toolbar color="primary">
            <v-row>
                <v-col>
                    <v-tabs v-model="selectedTab" fixed-tabs hide-slider bg-color="blue">
                        <v-tab v-for="tab in tabs" :key="tab.id" :class="{
                            'blue-bg': selectedTab !== tab.value,
                            'surface-bg': selectedTab === tab.value,
                        }" @click="selectedTab = tab.value">
                            <v-toolbar-title>{{ tab.label }}</v-toolbar-title>
                        </v-tab>
                    </v-tabs>
                </v-col>
            </v-row>
        </v-toolbar>
    </v-card>

    <v-card-text class="d-flex justify-space-between mx-0 bg-surface mb-5">
        <v-container>
            <v-row justify="center">
                <v-col cols="12">
                    <div v-if="selectedTab === 'colecoes'">
                     <Colecao />
                    </div>
                    <div v-if="selectedTab === 'autores'">
                     <Autor />
                    </div>
                    <div v-if="selectedTab === 'livros'">
                     <Livro />
                    </div>
                    <div v-if="selectedTab === 'leitores'">
                     <Leitor />
                    </div>
                </v-col>
            </v-row>
        </v-container>
    </v-card-text>

</template>
