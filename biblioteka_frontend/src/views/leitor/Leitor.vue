<script setup lang="ts">
import type { Pagination } from '../../api/adapters/BaseAdapters';
import type { InfoDataTableServer, Leitor } from '../../types';
import type { DataTableHeader } from '../../types/vuetify';
import { reactive, watchEffect } from 'vue';
import { useNotificationStore } from '../../stores/Notification';
import DialogDelete from '@/components/DialogDelete.vue';
import LeitorService from '@/api/LeitorService';
import LeitorEmprestimos from './LeitorEmprestimos.vue';
import LeitorForm from './LeitorForm.vue';
import LeitorReservas from './LeitorReservas.vue';

const constant: {
    cabecalhoLeitores: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoLeitores: [
        { title: "Nome Completo", align: "center", key: "nomeCompleto", sortable: true },
        { title: "CPF", align: "center", key: "cpf", sortable: true },
        { title: "E-mail", align: "center", key: "email", sortable: true },
        { title: "Telefone", align: "center", key: "telefone", sortable: true },
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
    listaLeitores: [] as Leitor[],
    infoDataTableServer: {} as InfoDataTableServer,
    idLeitor: null as unknown as number,
    exibirEmprestimosLeitor: false as boolean,
    exibirReservasLeitor: false as boolean
})

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    LeitorService.findSearch(page, itemsPerPage, sortBy, search)
        .then(({ items: leitores, pagination: page }) => {
            state.listaLeitores = leitores
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

function abrirDialogForm(idLeitor: number) {
    state.idLeitor = idLeitor;
    state.dialogVisible = true;
}

function abrirDialogDelete(id: number) {
    state.idLeitor = id;
    atualizarDialogDelete();
}

async function deletarItem(id: number) {
    try {
        await LeitorService.delete(id);
        loadItems(state.infoDataTableServer);
        atualizarDialogDelete();
        constant.notificationStore.notificar({ mensagem: "Leitor excluído com sucesso", tipoMensagem: "success", visibilidade: true })
    } catch (err) {
        constant.notificationStore.notificar({ mensagem: "Erro ao excluir o leitor!", tipoMensagem: "error", visibilidade: true })
    }
}
function exibirEmprestimosLeitor(idLeitor: number) {
    state.exibirEmprestimosLeitor = true;
    state.idLeitor = idLeitor;
}

function exibirReservasLeitor(idLeitor: number) {
    state.exibirReservasLeitor = true;
    state.idLeitor = idLeitor;
}

const voltarParaLeitores = () => {
    state.exibirEmprestimosLeitor = false;
    state.exibirReservasLeitor = false;
};

function formatarCPF(cpf: string): string {
    const formattedCPF = cpf.replace(
        /(\d{3})(\d{3})(\d{3})(\d{2})/,
        '$1.$2.$3-$4'
    );
    return formattedCPF;
}

function formatarTelefone(telefone: string): string {
    const formattedTelefone = telefone.replace(
        /(\d{2})(\d{5})(\d{4})/,
        '($1) $2-$3'
    );
    return formattedTelefone;
}

</script>

<template>

    <leitor-emprestimos v-if="state.exibirEmprestimosLeitor" :dialog-visible="state.dialogVisible"
        :leitor-id="state.idLeitor" @submitted="atualizarQuandoFormEnviado" @canceled="fecharModal"
        @voltar-para-leitores="voltarParaLeitores" />

    <leitor-reservas v-else-if="state.exibirReservasLeitor" :dialog-visible="state.dialogVisible" :leitor-id="state.idLeitor"
        @submitted="atualizarQuandoFormEnviado" @canceled="fecharModal" @voltar-para-leitores="voltarParaLeitores" />

    <v-card-text v-else>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoLeitores" :items="state.listaLeitores"
            :items-per-page="state.pagination.pageSize" :items-length="state.pagination.total"
            :items-per-page-options="constant.itemsPerPageOptions" @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model.trim="state.search"
                        :flat="true" label="Filtrar Leitores" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="blue" elevation="0"
                            @click="abrirDialogForm(null as unknown as number)">
                            Adicionar leitor
                        </v-btn>
                    </div>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr class="text-center">
                    <td>{{ item.nomeCompleto }}</td>
                    <td>{{ formatarCPF(item.cpf) }}</td>
                    <td>{{ item.usuario.email }}</td>
                    <td>{{ formatarTelefone(item.telefone) }}</td>
                    <td>
                        <v-tooltip text="Empréstimos" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn icon="mdi-book" variant="text" color="info"
                                    @click="exibirEmprestimosLeitor(item.id)" v-bind="props"></v-btn>
                            </template>
                        </v-tooltip>
                        <v-tooltip text="Reservas" location="top">
                            <template v-slot:activator="{ props }">
                                <v-btn icon="mdi-book" variant="text" color="black"
                                    @click="exibirReservasLeitor(item.id)" v-bind="props"></v-btn>
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

    <leitor-form :dialog-visible="state.dialogVisible" :leitor-id="state.idLeitor"
        @submitted="atualizarQuandoFormEnviado" @canceled="fecharModal" />

    <dialog-delete v-model:dialog-visible="state.dialogDelete" @canceled="atualizarDialogDelete()"
        @submitted="deletarItem(state.idLeitor)"
        :descricao="`Você tem certeza que deseja excluir esse leitor?`"></dialog-delete>

</template>
