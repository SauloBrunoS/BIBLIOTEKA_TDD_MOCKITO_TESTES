<script setup lang="ts">
import type { Pagination } from '../../api/adapters/BaseAdapters';
import type { InfoDataTableServer, Emprestimo, Leitor } from '../../types';
import type { DataTableHeader } from '../../types/vuetify';
import { reactive, watchEffect } from 'vue';
import { useNotificationStore } from '../../stores/Notification';
import dayjs from 'dayjs';
import LeitorService from '@/api/LeitorService';
import { LivroDevolvido } from '@/types/enum';
import EmprestimoForm from '../emprestimo/EmprestimoForm.vue';
import DevolucaoForm from '../emprestimo/DevolucaoForm.vue';
import RenovacaoForm from '../emprestimo/RenovacaoForm.vue';
import EmprestimoService from '@/api/EmprestimoService';

const constant: {
    cabecalhoEmprestimos: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoEmprestimos: [
        { title: "Código do Empréstimo", align: "center", key: "id", sortable: true },
        { title: "Leitor", align: "center", key: "e.leitor.cpf", sortable: true },
        { title: "Data de Empréstimo", align: "center", key: "dataEmprestimo", sortable: true },
        { title: "Data Limite", align: "center", key: "dataLimite", sortable: true },
        { title: "Data de Devolução", align: "center", key: "dataDevolucao", sortable: true },
        { title: "Quantidade de Renovações", align: "center", key: "quantidadeRenovacoes", sortable: true },
        { title: "Valor Base", align: "center", key: "valorBase", sortable: true },
        { title: "Multa", align: "center", key: "multa", sortable: true },
        { title: "Valor Total", align: "center", key: "valorTotal", sortable: true },
        { title: "Devolvido", align: "center", key: "devolvido", sortable: false },
        { title: "Código da Reserva", align: "center", key: "e.reserva.id", sortable: true },
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
    listaLeitores: [] as Leitor[],
    listaEmprestimos: [] as Emprestimo[],
    infoDataTableServer: {} as InfoDataTableServer,
    idEmprestimo: null as unknown as number,
    leitor: null as unknown as Leitor,
    dialogVisibleDevolucao: false as boolean,
    dialogVisibleRenovacao: false as boolean,
    devolvido: null as unknown as boolean
})

const props = defineProps<{
    livroId: number
}>();

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    EmprestimoService.findSearchEmprestimosByLivroId(page, itemsPerPage, sortBy, search, props.livroId, state.leitor?.id, state.devolvido)
        .then(({ items: emprestimos, pagination: page }) => {
            state.listaEmprestimos = emprestimos
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

function fecharDevolucaoModal() {
    state.dialogVisibleDevolucao = false;
}

function fecharRenovacaoModal() {
    state.dialogVisibleRenovacao = false;
}

function atualizarQuandoFormEnviado() {
    fecharModal();
    atualizar();
}

function atualizarQuandoFormDevolucaoEnviado() {
    fecharDevolucaoModal();
    atualizar();
}

function atualizarQuandoFormRenovacaoEnviado() {
    fecharRenovacaoModal();
    atualizar();
}

function abrirDialogForm(idEmprestimo: number) {
    state.idEmprestimo = idEmprestimo;
    state.dialogVisible = true;
}

function abrirDialogDevolucaoForm(idEmprestimo: number) {
    state.idEmprestimo = idEmprestimo;
    state.dialogVisibleDevolucao = true;
}

function abrirDialogRenovacaoForm(idEmprestimo: number) {
    state.idEmprestimo = idEmprestimo;
    state.dialogVisibleRenovacao = true;
}

watchEffect(() => {
    loadItems(state.infoDataTableServer);
})

const emit = defineEmits(['voltar-para-livros']);

const voltarParaLivros = () => {
    emit('voltar-para-livros');
};

async function buscarLeitores(leitoresSearch: string) {
    if (leitoresSearch != '') {
        try {
            const encodeUriSearch = encodeURIComponent(leitoresSearch);
            const leitoresList = await LeitorService.findAllLeitoresWithCPFFilter(encodeUriSearch);
            state.listaLeitores = leitoresList;
        } catch (error) {
            console.error("Erro ao buscar a lista de leitores:", error);
        }
    } else if (state.listaLeitores.length > 0) {
        state.listaLeitores = [];
    }
}

function createOptions<T extends Record<string, string>>(enumObject: T) {
    return Object.entries(enumObject).map(([key, value]) => {
        let booleanValue: boolean | null;

        switch (key) {
            case 'DEVOLVIDO':
                booleanValue = true;
                break;
            case 'NAO_DEVOLVIDO':
                booleanValue = false;
                break;
            case 'NENHUM':
                booleanValue = null;
                break;
            default:
                booleanValue = null;
        }

        return {
            text: value,
            value: booleanValue
        };
    });
}

const devolvidoOptions = createOptions(LivroDevolvido)

</script>

<template>
    <v-card-text>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoEmprestimos"
            :items="state.listaEmprestimos" :items-per-page="state.pagination.pageSize"
            :items-length="state.pagination.total" :items-per-page-options="constant.itemsPerPageOptions"
            @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center mb-5">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model.trim="state.search"
                        :flat="true" label="Filtrar Empréstimos" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <div class="mr-2">
                        <v-autocomplete class="mt-6" v-model="state.leitor" width="200px" :items="state.listaLeitores"
                            item-title="cpf" item-value="id" return-object label="Filtrar por CPF do leitor"
                            variant="outlined" @update:search="buscarLeitores"
                            no-data-text="Digite algum cpf para buscar os leitores"></v-autocomplete>
                    </div>
                    <div class="mr-2">
                        <v-select class="mt-6" v-model="state.devolvido" width="200px" :items="devolvidoOptions"
                            item-title="text" item-value="value" label="Filtrar por Devolução"
                            variant="outlined"></v-select>
                    </div>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="blue" elevation="0"
                            @click="abrirDialogForm(null as unknown as number)">
                            Adicionar Empréstimo
                        </v-btn>
                    </div>
                    <v-btn @click="voltarParaLivros" color="error" class="px-3 py-7 d-flex align-center">VOLTAR</v-btn>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr class="text-center">
                    <td>{{ item.id }}</td>
                    <td>{{ item.leitor.cpf }}</td>
                    <td>{{ dayjs(item.dataEmprestimo).format("DD/MM/YYYY") }}</td>
                    <td>{{ dayjs(item.dataLimite).format("DD/MM/YYYY") }}</td>
                    <td>{{ item.dataDevolucao != null ? dayjs(item.dataDevolucao).format("DD/MM/YYYY") : "A Definir" }}
                    </td>
                    <td>{{ item.quantidadeRenovacoes }}</td>
                    <td>{{ item.valorBase }}</td>
                    <td>{{ item.multa }}</td>
                    <td>{{ item.valorTotal }}</td>
                    <td>{{ item.devolvido == true ? "Devolvido" : "Não Devolvido" }}</td>
                    <td>{{ item.reserva != null ? item.reserva.id : "Não possui" }}</td>
                    <td>
                        <v-tooltip text="Devolver Livro" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn icon="mdi-book" variant="text" color="yellow"
                                    @click="abrirDialogDevolucaoForm(item.id)" v-bind="props"></v-btn>
                            </template>
                        </v-tooltip>
                        <v-tooltip text="Renovar Empréstimo" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn icon="mdi-book" variant="text" color="orange"
                                    @click="abrirDialogRenovacaoForm(item.id)" v-bind="props"></v-btn>
                            </template>
                        </v-tooltip>
                    </td>
                </tr>
            </template>
        </v-data-table-server>
    </v-card-text>

    <EmprestimoForm :dialog-visible="state.dialogVisible" :emprestimo-id="state.idEmprestimo" :livro-id="props.livroId"
        @submitted="atualizarQuandoFormEnviado" @canceled="fecharModal" />

    <DevolucaoForm :dialog-visible="state.dialogVisibleDevolucao" :emprestimo-id="state.idEmprestimo"
        @submitted="atualizarQuandoFormDevolucaoEnviado" @canceled="fecharDevolucaoModal" />

    <RenovacaoForm :dialog-visible="state.dialogVisibleRenovacao" :emprestimo-id="state.idEmprestimo"
        @submitted="atualizarQuandoFormRenovacaoEnviado" @canceled="fecharRenovacaoModal" />

</template>