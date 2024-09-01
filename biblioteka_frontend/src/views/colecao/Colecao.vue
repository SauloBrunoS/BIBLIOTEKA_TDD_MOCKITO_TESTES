<script setup lang="ts">
import type { Pagination } from '../../api/adapters/BaseAdapters';
import type { Colecao, InfoDataTableServer } from '../../types';
import type { DataTableHeader } from '../../types/vuetify';
import { reactive } from 'vue';
import { useNotificationStore } from '../../stores/Notification';
import ColecaoService from '../../api/ColecaoService';
import ColecaoForm from './ColecaoForm.vue';
import DialogDelete from '@/components/DialogDelete.vue';
import ColecaoDetalhes from './ColecaoDetalhes.vue';

const constant: {
    cabecalhoColecoes: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoColecoes: [
        { title: "Nome", key: "nome", align: "center", sortable: true },
        { title: "Descrição", key: "descricao", align: "center", sortable: true },
        { title: "", key: "id", sortable: false }
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
    listaColecoes: [] as Colecao[],
    infoDataTableServer: {} as InfoDataTableServer,
    idColecao: null as unknown as number,
    exibirDetalhesColecao: false as boolean
})

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    ColecaoService.findSearch(page, itemsPerPage, sortBy, search)
        .then(({ items: colecoes, pagination: page }) => {
            state.listaColecoes = colecoes
            state.pagination.page = page.pageable.number
            state.pagination.total = page.totalElements
            state.pagination.pageCount = page.totalPages
        });
}

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

function abrirDialogForm(idColecao: number) {
    state.idColecao = idColecao;
    state.dialogVisible = true;
}

function abrirDialogDelete(id: number) {
    state.idColecao = id;
    atualizarDialogDelete();
}

async function deletarItem(id: number) {
    try {
        await ColecaoService.delete(id);
        loadItems(state.infoDataTableServer);
        atualizarDialogDelete();
        constant.notificationStore.notificar({ mensagem: "Coleção excluída com sucesso", tipoMensagem: "success", visibilidade: true })
    } catch (err) {
        constant.notificationStore.notificar({ mensagem: "Erro ao excluir a Coleção!", tipoMensagem: "error", visibilidade: true })
    }
}

function exibirDetalhesColecao(idColecao: number) {
  state.exibirDetalhesColecao = true;
  state.idColecao = idColecao;
}

const voltarParaColecoes = () => {
  state.exibirDetalhesColecao = false;
};

</script>

<template>
  <colecao-detalhes v-if="state.exibirDetalhesColecao" :colecao-id="state.idColecao"
    @voltar-para-colecoes="voltarParaColecoes()" />
    <v-card-text v-else>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoColecoes" :items="state.listaColecoes"
            :items-per-page="state.pagination.pageSize" :items-length="state.pagination.total"
            :items-per-page-options="constant.itemsPerPageOptions" @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model.trim="state.search"
                        :flat="true" label="Filtrar Coleções" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="blue" elevation="0"
                            @click="abrirDialogForm(null as unknown as number)">
                            Adicionar coleção
                        </v-btn>
                    </div>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr>
                    <td>{{ item.nome }}</td>
                    <td>{{ item.descricao }}</td>
                    <td>
                        <v-tooltip text="Detalhes da Coleção" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn-details
                                    @click="exibirDetalhesColecao(item.id)"
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
        @submitted="deletarItem(state.idColecao)"
        :descricao="`Você tem certeza que deseja excluir essa coleção?`"></dialog-delete>

    <colecao-form :dialog-visible="state.dialogVisible" :colecao-id="state.idColecao"
        @submitted="atualizarQuandoFormEnviado" @canceled="fecharModal" />
</template>
