<script setup lang="ts">
import type { Pagination } from '../../api/adapters/BaseAdapters';
import type { InfoDataTableServer, Livro, Reserva } from '../../types';
import type { DataTableHeader } from '../../types/vuetify';
import { reactive, watchEffect } from 'vue';
import { useNotificationStore } from '../../stores/Notification';
import dayjs from 'dayjs';
import LivroService from '@/api/LivroService';
import { StatusReserva } from '@/types/enum';
import ReservaService from '@/api/ReservaService';
import ReservaForm from '../autor/reservas/ReservaForm.vue';
import CancelarReservaForm from './CancelarReservaForm.vue';

const constant: {
    cabecalhoReservas: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoReservas: [
        { title: "Código da Reserva", align: "center", key: "id", sortable: true },
        { title: "Livro", align: "center", key: "r.livro.isbn", sortable: true },
        { title: "Data de Cadastro", align: "center", key: "dataCadastro", sortable: true },
        { title: "Data Limite", align: "center", key: "dataLimite", sortable: true },
        { title: "Status", align: "center", key: "status", sortable: false },
        { title: "Código do Empréstimo", align: "center", key: "r.emprestimo.id", sortable: true },
        { title: "", align: "center", key: "id", sortable: false },
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
    listaLivros: [] as Livro[],
    listaReservas: [] as Reserva[],
    infoDataTableServer: {} as InfoDataTableServer,
    idReserva: null as unknown as number,
    livro: null as unknown as Livro,
    dialogVisibleCancelar: false as boolean,
    selectedStatus: null as unknown as keyof typeof StatusReserva
})

const props = defineProps<{
    leitorId: number
}>();

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    ReservaService.findSearchReservasByLeitorId(page, itemsPerPage, sortBy, search, props.leitorId, state.livro?.id, state.selectedStatus)
        .then(({ items: reservas, pagination: page }) => {
            state.listaReservas = reservas
            console.log(state.listaReservas)
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

function fecharCancelarModal() {
    state.dialogVisibleCancelar = false;
}

function atualizarQuandoFormEnviado() {
    fecharModal();
    atualizar();
}

function atualizarQuandoFormCancelarEnviado() {
    fecharCancelarModal();
    atualizar();
}

function abrirDialogForm(idReserva: number) {
    state.idReserva = idReserva;
    state.dialogVisible = true;
}

function abrirDialogCancelaroForm(idReserva: number) {
    state.idReserva = idReserva;
    state.dialogVisibleCancelar = true;
}

watchEffect(() => {
    loadItems(state.infoDataTableServer);
})

const emit = defineEmits(['voltar-para-leitores']);

const voltarParaLeitores = () => {
    emit('voltar-para-leitores');
};

async function buscarLivros(livrosSearch: string) {
    if (livrosSearch != '') {
        try {
            const encodeUriSearch = encodeURIComponent(livrosSearch);
            const livrosList = await LivroService.findAllLivrosWithISBNFilter(encodeUriSearch);
            state.listaLivros = livrosList;
        } catch (error) {
            console.error("Erro ao buscar a lista de livros:", error);
        }
    } else if (state.listaLivros.length > 0) {
        state.listaLivros = [];
    }
}

function createOptions<T>(enumObject: T) {
    return Object.keys(enumObject).map(key => ({
        text: enumObject[key as keyof T],
        value: key == "NENHUM" ? null : key
    }));
}

const statusReservaOptions = createOptions(StatusReserva);

</script>

<template>
    <v-card-text>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoReservas" :items="state.listaReservas"
            :items-per-page="state.pagination.pageSize" :items-length="state.pagination.total"
            :items-per-page-options="constant.itemsPerPageOptions" @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center mb-5">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model.trim="state.search"
                        :flat="true" label="Filtrar Reservas" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <div class="mr-2">
                        <v-autocomplete class="mt-6" v-model="state.livro" width="200px" :items="state.listaLivros"
                            item-title="isbn" item-value="id" return-object label="Filtrar por ISBN do Livro"
                            variant="outlined" @update:search="buscarLivros"
                            no-data-text="Digite algum ISBN para buscar os livros"></v-autocomplete>
                    </div>
                    <div class="mr-2">
                        <v-select class="mt-6" v-model="state.selectedStatus" width="200px"
                            :items="statusReservaOptions" item-title="text" item-value="value"
                            label="Filtrar por Status" variant="outlined"></v-select>
                    </div>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="blue" elevation="0"
                            @click="abrirDialogForm(null as unknown as number)">
                            Adicionar Reserva
                        </v-btn>
                    </div>
                    <v-btn @click="voltarParaLeitores" color="error"
                        class="px-3 py-7 d-flex align-center">VOLTAR</v-btn>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr class="text-center">
                    <td>{{ item.id }}</td>
                    <td>{{ item.livro.isbn }}</td>
                    <td>{{ dayjs(item.dataCadastro).format("DD/MM/YYYY") }}</td>
                    <td>{{ item.dataLimite != null ? dayjs(item.dataLimite).format("DD/MM/YYYY") : "A Definir" }}
                    </td>
                    <td>{{ StatusReserva[item.status] }}</td>
                    <td>{{ item.emprestimo != null ? item.emprestimo.id : "Não possui" }}</td>
                    <td>
                        <v-tooltip text="Cancelar Reserva" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn icon="mdi-book" variant="text" color="error"
                                    @click="abrirDialogCancelaroForm(item.id)" v-bind="props"></v-btn>
                            </template>
                        </v-tooltip>
                    </td>
                </tr>
            </template>
        </v-data-table-server>
    </v-card-text>

    <ReservaForm :dialog-visible="state.dialogVisible" :leitor-id="props.leitorId"
        @submitted="atualizarQuandoFormEnviado" @canceled="fecharModal" />

    <CancelarReservaForm :dialog-visible="state.dialogVisibleCancelar" :reserva-id="state.idReserva"
        @submitted="atualizarQuandoFormCancelarEnviado" @canceled="fecharCancelarModal" />

</template>