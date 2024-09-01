<script lang="ts" setup>
import { onMounted, reactive, watch } from "vue";
import { Form, Field } from 'vee-validate';
import { useNotificationStore } from '@/stores/Notification';
import { Autor, Colecao, Livro } from '@/types';
import AutorService from "@/api/AutorService";
import dayjs from 'dayjs';
import DatePicker from '@/components/DatePicker.vue';
import LivroService from "@/api/LivroService";
import ColecaoService from "@/api/ColecaoService";
const emit = defineEmits(['submitted', 'canceled'])

const props = defineProps<{
    dialogVisible: boolean,
    livroId: number,
    colecaoId?: number,
    autorId?: number
}>();

const state = reactive({
    livro: {} as Livro,
    autor: null as unknown as Autor,
    colecao: null as unknown as Colecao,
    listaColecoesParaAdicionar: [] as Colecao[],
    listaAutores: [] as Autor[],
    listaColecoes: [] as Colecao[],
    listaAutoresParaAdicionar: [] as Autor[],
    dialog: false as boolean,
    showDatePicker: false as boolean
})

const constant = {
    notificationStore: useNotificationStore()
}

function modoEdicao() {
    return !!props.livroId;
}

async function loadLivro() {
    if (props.livroId == null) state.livro = {} as Livro;
    else {
        try {
            const livro = await LivroService.getById(props.livroId);
            state.livro = livro;
            state.listaColecoesParaAdicionar = livro.colecoes;
            state.listaAutoresParaAdicionar = livro.autores;
        } catch (error) {
            console.error("Erro ao carregar detalhes do livro: ", error);
        }
    }
}

async function onSubmit(values: any, actions: any) {

    values.dataPublicacao = dayjs(state.livro.dataPublicacao).format("YYYY-MM-DD");
    values.autores = state.listaAutoresParaAdicionar.map(item => ({ id: item.id }));;
    values.colecoes = state.listaColecoesParaAdicionar.map(item => ({ id: item.id }));;

    if (modoEdicao()) {
        try {
            await LivroService.update(values, props.livroId);
        }
        catch (err) {
            console.error("Erro ao alterar livro:", err);
            constant.notificationStore.notificar({ mensagem: "Erro ao alterar livro!", tipoMensagem: "error", visibilidade: true })
        }
    } else {
        try {
            values.numeroCopiasDisponiveis = values.numeroCopiasTotais;
            await LivroService.create(values);
        } catch (err) {
            console.error("Erro ao cadastrar novo livro:", err);
            constant.notificationStore.notificar({ mensagem: "Erro ao cadastrar novo livro!", tipoMensagem: "error", visibilidade: true })
        }
    }
    limparItens();
    emit('submitted');
    actions.resetForm();
}

function limparItens() {
    state.listaColecoesParaAdicionar = [] as Colecao[];
    state.listaAutoresParaAdicionar = [] as Autor[];
    state.colecao = null as unknown as Colecao;
    state.autor = null as unknown as Autor;
}

const enterPress = (event: KeyboardEvent) => {
    if (event.key === "Enter") {
        event.preventDefault();
    }
};

watch(
    () => props.dialogVisible,
    (novoDialogVisible) => {
        state.dialog = novoDialogVisible;
        if (novoDialogVisible) loadLivro();
    },
);

async function cancel() {
    limparItens();
    if (props.autorId != null || props.colecaoId != null) {
        await carregarAutorEColecao();
    }
    emit("canceled");
}

function addAutor(autor: Autor) {
    if (autor == null) {
        constant.notificationStore.notificar({
            mensagem: "Não pode adicionar um autor vazio!",
            tipoMensagem: "error",
            visibilidade: true
        })
    } else {
        state.listaAutoresParaAdicionar.push(autor);
        state.autor = null as unknown as Autor;
    }
}

function addColecao(colecao: Colecao) {
    if (colecao == null) {
        constant.notificationStore.notificar({
            mensagem: "Não pode adicionar uma coleção vazia!",
            tipoMensagem: "error",
            visibilidade: true
        })
    } else {
        state.listaColecoesParaAdicionar.push(colecao);
        state.colecao = null as unknown as Colecao;
    }
}


async function buscarAutores(autoresSearch: string) {
    if (autoresSearch != '') {
        try {
            const encodeUriSearch = encodeURIComponent(autoresSearch);
            const autoresList = await AutorService.findAllAutoresWithNomeFilter(encodeUriSearch);
            state.listaAutores = autoresList;
        } catch (error) {
            console.error("Erro ao buscar a lista de autores:", error);
        }
    } else if (state.listaAutores.length > 0) {
        state.listaAutores = [];
    }
}

function removeAutor(autor: Autor) {
    const index = state.listaAutoresParaAdicionar.indexOf(autor);
    state.listaAutoresParaAdicionar.splice(index, 1);
}

function removeColecao(colecao: Colecao) {
    const index = state.listaColecoesParaAdicionar.indexOf(colecao);
    state.listaColecoesParaAdicionar.splice(index, 1);
}

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

onMounted(() => {
    document.addEventListener("keydown", enterPress);
});

async function carregarAutorEColecao (){
    if (props.autorId != null) {
        const autor = await AutorService.getById(props.autorId);
        state.listaAutoresParaAdicionar.push(autor);
    }
    if (props.colecaoId != null) {
        const colecao = await ColecaoService.getById(props.colecaoId);
        state.listaColecoesParaAdicionar.push(colecao);
    }
}

onMounted(async () => {
    await carregarAutorEColecao();
});


</script>

<template>
    <v-dialog v-model="state.dialog" no-click-outside persistent width="60%">
        <v-card>
            <v-card-title class="mx-auto my-4">
                <span class="text-h5">{{ modoEdicao() ? 'Editar' : 'Cadastrar' }} Livro</span>
            </v-card-title>
            <v-card-text>
                <Form @submit="onSubmit" @reset="cancel()">
                    <v-container>
                        <v-row>
                            <v-col cols="3">
                                <Field name="isbn" v-model.trim="state.livro.isbn" v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" label="ISBN" variant="outlined"
                                        v-model="state.livro.isbn" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="5">
                                <Field name="titulo" v-model="state.livro.titulo" v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" v-model="state.livro.titulo" :error-messages="errors"
                                        label="Título" variant="outlined" />
                                </Field>
                            </v-col>
                            <v-col cols="4">
                                <Field name="dataPublicacao" v-model="state.livro.dataPublicacao"
                                    v-slot="{ field, errors }">
                                    <date-picker v-model="state.livro.dataPublicacao" v-bind="{ field }"
                                        :error-messages="errors" label="Data de Publicação"
                                        @update:modelValue="(newValue) => state.livro.dataPublicacao = newValue" />
                                </Field>
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="5">
                                <Field name="colecoes" v-model.trim="state.colecao" v-slot="{ field, errors }">
                                    <v-autocomplete v-model="state.colecao" :items="state.listaColecoes"
                                        item-title="nome" item-value="id" return-object label="Coleção"
                                        variant="outlined" @update:search="buscarColecoes"
                                        no-data-text="Digite algo para buscar as coleções" v-bind="{ field }"
                                        :error-messages="errors"></v-autocomplete>
                                </Field>
                            </v-col>
                            <v-col>
                                <v-btn class="px-4 py-7 d-flex justify-content align-center" color="primary"
                                    elevation="0" @click="addColecao(state.colecao)">
                                    Adicionar
                                </v-btn>
                            </v-col>
                        </v-row>
                        <v-row class="my-0 pa-0">
                            <v-col cols="5">
                                <v-chip v-for="colecao in state.listaColecoesParaAdicionar" :key="colecao.id" closable
                                    @click:close="removeColecao(colecao)" class="ma-1">
                                    {{ colecao.nome }}
                                </v-chip>
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="5">
                                <Field name="autores" v-model.trim="state.autor" v-slot="{ field, errors }">
                                    <v-autocomplete v-model="state.autor" :items="state.listaAutores"
                                        item-title="nomeCompleto" item-value="id" return-object label="Autor"
                                        variant="outlined" @update:search="buscarAutores"
                                        no-data-text="Digite algo para buscar os autores" v-bind="{ field }"
                                        :error-messages="errors"></v-autocomplete>
                                </Field>
                            </v-col>
                            <v-col>
                                <v-btn class="px-4 py-7 d-flex justify-content align-center" color="primary"
                                    elevation="0" @click="addAutor(state.autor)">
                                    Adicionar
                                </v-btn>
                            </v-col>
                        </v-row>
                        <v-row class="my-0 pa-0">
                            <v-col cols="5">
                                <v-chip v-for="autor in state.listaAutoresParaAdicionar" :key="autor.id" closable
                                    @click:close="removeAutor(autor)" class="ma-1">
                                    {{ autor.nomeCompleto }}
                                </v-chip>
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="3">
                                <Field name="qtdPaginas" v-model="state.livro.qtdPaginas" v-slot="{ field, errors }">
                                    <v-text-field type="number" v-bind="field" label="Quantidade de Páginas"
                                        variant="outlined" v-model="state.livro.qtdPaginas" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="4">
                                <Field name="numeroCopiasTotais" v-model="state.livro.numeroCopiasTotais"
                                    v-slot="{ field, errors }">
                                    <v-text-field type="number" v-bind="field" label="Número de Cópias Totais"
                                        variant="outlined" v-model="state.livro.numeroCopiasTotais"
                                        :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                        </v-row>
                        <v-card-actions>
                            <v-spacer></v-spacer>
                            <v-btn class="mb-6" color="grey-darken-4" variant="outlined" type="reset">
                                Cancelar
                            </v-btn>
                            <v-btn class="mb-6" color="primary" variant="flat" type="submit" data-cy="btnSalvar">
                                Salvar
                            </v-btn>
                        </v-card-actions>
                    </v-container>
                </Form>
            </v-card-text>
        </v-card>
    </v-dialog>
</template>
