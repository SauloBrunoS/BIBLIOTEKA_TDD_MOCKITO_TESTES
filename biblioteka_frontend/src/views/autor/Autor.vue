<script setup lang="ts">
import type { Pagination } from '../../api/adapters/BaseAdapters';
import type { Autor, InfoDataTableServer } from '../../types';
import type { DataTableHeader } from '../../types/vuetify';
import { reactive, watchEffect } from 'vue';
import { useNotificationStore } from '../../stores/Notification';
import DialogDelete from '@/components/DialogDelete.vue';
import AutorService from '@/api/AutorService';
import AutorForm from './AutorForm.vue';
import { Nacionalidade } from '@/types/enum';
import dayjs from 'dayjs';
import AutorDetalhes from './AutorDetalhes.vue';

const constant: {
    cabecalhoAutores: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoAutores: [
        { title: "Nome Completo", align: "center", key: "nomeCompleto", sortable: true },
        { title: "Data de Nascimento", align: "center", key: "dataNascimento", sortable: true },
        { title: "Nacionalidade", align: "center", key: "nacionalidade", sortable: true },
        { title: "", align: "center", key: "id", sortable: false }
    ],
    itemsPerPageOptions: [
        { value: 5, title: '5' },
        { value: 10, title: '10' },
        { value: 25, title: '25' },
        { value: 50, title: '50' },
        { value: 100, title: '100' }
    ],
    notificationStore: useNotificationStore()
}

const state = reactive({
    pagination: { pageSize: 5 } as Pagination,
    search: "" as string,
    dialogVisible: false as boolean,
    dialogDelete: false,
    listaAutores: [] as Autor[],
    infoDataTableServer: {} as InfoDataTableServer,
    idAutor: null as unknown as number,
    nacionalidade: null as unknown as keyof typeof Nacionalidade,
    exibirDetalhesAutor: false as boolean
})

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    AutorService.findSearch(page, itemsPerPage, sortBy, search, state.nacionalidade)
        .then(({ items: autores, pagination: page }) => {
            state.listaAutores = autores
            state.pagination.page = page.pageable.number
            state.pagination.total = page.totalElements
            state.pagination.pageCount = page.totalPages
        });
}
watchEffect(() => {
    loadItems(state.infoDataTableServer);
})
function atualizar() {
    loadItems(state.infoDataTableServer);
}

function fecharModal() {
    state.dialogVisible = false;
}

function atualizarQuandoFormEnviado() {
    fecharModal();
    atualizar();
}

function atualizarDialogDelete() {
    state.dialogDelete = !state.dialogDelete;
}

function abrirDialogForm(idAutor: number) {
    state.idAutor = idAutor;
    state.dialogVisible = true;
}

function abrirDialogDelete(id: number) {
    state.idAutor = id;
    atualizarDialogDelete();
}

async function deletarItem(id: number) {
    try {
        await AutorService.delete(id);
        loadItems(state.infoDataTableServer);
        atualizarDialogDelete();
        constant.notificationStore.notificar({ mensagem: "Autor excluído com sucesso", tipoMensagem: "success", visibilidade: true })
    } catch (err) {
        constant.notificationStore.notificar({ mensagem: "Erro ao excluir o autor!", tipoMensagem: "error", visibilidade: true })
    }
}

function createOptions<T>(enumObject: T) {
    return Object.keys(enumObject).map(key => ({
        text: enumObject[key as keyof T],
        value: key === "NENHUM" ? null : key
    }));
}

const nacionalidadeOptions = createOptions(Nacionalidade)

function exibirDetalhesAutor(idAutor: number) {
  state.exibirDetalhesAutor = true;
  state.idAutor = idAutor;
}

const voltarParaAutores = () => {
  state.exibirDetalhesAutor = false;
};


</script>

<template>
    <autor-detalhes v-if="state.exibirDetalhesAutor" :autor-id="state.idAutor"
    @voltar-para-autores="voltarParaAutores()" />
   
    <v-card-text v-else>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoAutores" :items="state.listaAutores"
            :items-per-page="state.pagination.pageSize" :items-length="state.pagination.total"
            :items-per-page-options="constant.itemsPerPageOptions" @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model.trim="state.search"
                        :flat="true" label="Filtrar Autores" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <v-autocomplete class="mt-6" label="Filtrar por Nacionalidade" variant="outlined"
                        :items="nacionalidadeOptions" item-title="text" item-value="value"
                        v-model="state.nacionalidade" />
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="blue" elevation="0"
                            @click="abrirDialogForm(null as unknown as number)">
                            Adicionar autor
                        </v-btn>
                    </div>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr class="text-center">
                    <td>{{ item.nomeCompleto }}</td>
                    <td>{{ dayjs(item.dataNascimento).format("DD/MM/YYYY") }}</td>
                    <td>{{ Nacionalidade[item.nacionalidade as keyof typeof Nacionalidade] }}</td>
                    <td>
                        <v-tooltip text="Detalhes do Autor" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn-details
                                    @click="exibirDetalhesAutor(item.id)"
                                    v-bind="props"></v-btn-details>
                            </template>
                        </v-tooltip>
                        <v-tooltip text="Editar" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn icon="mdi-pencil" variant="text" color="info" @click="abrirDialogForm(item.id)"
                                    v-bind="props"></v-btn>
                            </template>
                        </v-tooltip>
                        <v-tooltip text="Excluir" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn-delete @click="abrirDialogDelete(item.id)" v-bind="props"></v-btn-delete>
                            </template>
                        </v-tooltip>
                    </td>
                </tr>
            </template>
        </v-data-table-server>
    </v-card-text>

    <dialog-delete v-model:dialog-visible="state.dialogDelete" @canceled="atualizarDialogDelete()"
        @submitted="deletarItem(state.idAutor)"
        :descricao="`Você tem certeza que deseja excluir esse autor?`"></dialog-delete>

    <autor-form :dialog-visible="state.dialogVisible" :autor-id="state.idAutor" @submitted="atualizarQuandoFormEnviado"
        @canceled="fecharModal" />
</template>
