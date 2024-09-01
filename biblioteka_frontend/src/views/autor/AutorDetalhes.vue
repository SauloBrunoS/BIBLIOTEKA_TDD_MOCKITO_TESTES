<script setup lang="ts">
import type { Pagination } from '../../api/adapters/BaseAdapters';
import type { Livro, InfoDataTableServer, Colecao } from '../../types';
import type { DataTableHeader } from '../../types/vuetify';
import { reactive, watchEffect } from 'vue';
import { useNotificationStore } from '../../stores/Notification';
import DialogDelete from '@/components/DialogDelete.vue';
import dayjs from 'dayjs';
import LivroService from '@/api/LivroService';
import LivroForm from '../livro/LivroForm.vue';
import ColecaoService from '@/api/ColecaoService';
import AutorService from '@/api/AutorService';

const constant: {
    cabecalhoLivros: DataTableHeader[];
    itemsPerPageOptions: any[];
    notificationStore: any;
} = {
    cabecalhoLivros: [
        { title: "ISBN", align: "center", key: "isbn", sortable: true },
        { title: "Título", align: "center", key: "titulo", sortable: true },
        { title: "Autores", align: "center", key: "l.autores", sortable: false },
        { title: "Coleções", align: "center", key: "l.colecoes", sortable: false },
        { title: "Data de Publicação", align: "center", key: "dataPublicacao", sortable: true },
        { title: "Quantidade de Páginas", align: "center", key: "qtdPaginas", sortable: true },
        { title: "Quantidade de Cópias Disponíveis", align: "center", key: "numeroCopiasDisponiveis", sortable: true },
        { title: "Quantidade de Cópias Totais", align: "center", key: "numeroCopiasTotais", sortable: true },

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
    listaLivros: [] as Livro[],
    infoDataTableServer: {} as InfoDataTableServer,
    idLivro: null as unknown as number,
    colecao: null as unknown as Colecao,
    listaColecoes: [] as Colecao[]
})

const props = defineProps<{
    autorId: number
}>();

function loadItems({ search, page, itemsPerPage, sortBy }: InfoDataTableServer) {
    state.infoDataTableServer = { page, itemsPerPage, sortBy, search }
    AutorService.findSearchLivrosByAutorId(page, itemsPerPage, sortBy, search, props.autorId, state.colecao?.id)
        .then(({ items: livros, pagination: page }) => {
            state.listaLivros = livros
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

function abrirDialogForm(idLivro: number) {
    state.idLivro = idLivro;
    state.dialogVisible = true;
}

function abrirDialogDelete(id: number) {
    state.idLivro = id;
    atualizarDialogDelete();
}

watchEffect(() => {
    loadItems(state.infoDataTableServer);
})

async function deletarItem(id: number) {
    try {
        await LivroService.delete(id);
        loadItems(state.infoDataTableServer);
        atualizarDialogDelete();
        constant.notificationStore.notificar({ mensagem: "Livro excluído com sucesso", tipoMensagem: "success", visibilidade: true })
    } catch (err) {
        constant.notificationStore.notificar({ mensagem: "Erro ao excluir o livro!", tipoMensagem: "error", visibilidade: true })
    }
}

watchEffect(() => {
    loadItems(state.infoDataTableServer);
})

async function buscarColecoes(colecoesSearch: string) {
    if (colecoesSearch != '') {
        try {
            const encodeUriSearch = encodeURIComponent(colecoesSearch);
            const colecoesList = await ColecaoService.findAllColecoesWithNomeFilter(encodeUriSearch);
            state.listaColecoes = colecoesList;
        } catch (error) {
            console.error("Erro ao buscar a lista de coleções:", error);
        }
    } else if (state.listaColecoes.length > 0) {
        state.listaColecoes = [];
    }
}

const emit = defineEmits(['voltar-para-autores']);

const voltarParaAutores = () => {
  emit('voltar-para-autores');
};

</script>

<template>
    <v-card-text>
        <v-data-table-server :search="state.search" :headers="constant.cabecalhoLivros" :items="state.listaLivros"
            :items-per-page="state.pagination.pageSize" :items-length="state.pagination.total"
            :items-per-page-options="constant.itemsPerPageOptions" @update:options="loadItems">
            <template v-slot:top>
                <div class="d-flex justify-start align-center mb-5">
                    <v-text-field bg-color="background" class="mr-2 ml-2 mb-4 mt-4 w-50" v-model.trim="state.search"
                        :flat="true" label="Filtrar Livros" hide-details variant="solo" single-line>
                        <template #prepend-inner>
                            <div class="icon-container">
                                <v-icon>mdi-magnify</v-icon>
                            </div>
                        </template>
                    </v-text-field>
                    <div class="mr-2">
                        <v-autocomplete class="mt-6" v-model="state.colecao" width="200px" :items="state.listaColecoes"
                            item-title="nome" item-value="id" return-object label="Filtrar por Coleção"
                            variant="outlined" @update:search="buscarColecoes"
                            no-data-text="Digite algo para buscar as coleções"></v-autocomplete>
                    </div>
                    <div class="mr-4">
                        <v-btn class="mx-2 px-2 py-7 d-flex justify-content align-center" color="blue" elevation="0"
                            @click="abrirDialogForm(null as unknown as number)">
                            Adicionar livro
                        </v-btn>
                    </div>
                    <v-btn @click="voltarParaAutores" color="error" class="px-3 py-7 d-flex align-center">VOLTAR</v-btn>
                </div>
            </template>
            <template v-slot:item="{ item }">
                <tr class="text-center">
                    <td>{{ item.isbn }}</td>
                    <td>{{ item.titulo }}</td>
                    <td>{{ item.autores.map(a => a.nomeCompleto).join(', ') }}</td> 
                    <td>{{ item.colecoes.map(c => c.nome).join(', ') }}</td> 
                   
                    <td>{{ dayjs(item.dataPublicacao).format("DD/MM/YYYY") }}</td>
                    <td>{{ item.qtdPaginas }}</td>
                    <td>{{ item.numeroCopiasDisponiveis }}</td>
                    <td>{{ item.numeroCopiasTotais }}</td>

                    <td>
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

    <livro-form :dialog-visible="state.dialogVisible" :livro-id="state.idLivro" :autor-id="props.autorId" @submitted="atualizarQuandoFormEnviado"
        @canceled="fecharModal" />

    <dialog-delete v-model:dialog-visible="state.dialogDelete" @canceled="atualizarDialogDelete()"
        @submitted="deletarItem(state.idLivro)"
        :descricao="`Você tem certeza que deseja excluir esse livro?`"></dialog-delete>

</template>