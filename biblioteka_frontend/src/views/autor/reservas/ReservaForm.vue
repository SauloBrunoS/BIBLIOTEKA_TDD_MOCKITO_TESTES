<script lang="ts" setup>
import { reactive, watch } from "vue";
import { Form, Field } from 'vee-validate';
import { useNotificationStore } from '@/stores/Notification';
import { Leitor, Livro, Reserva } from '@/types';
import LivroService from "@/api/LivroService";
import LeitorService from "@/api/LeitorService";
import ReservaService from "@/api/ReservaService";

const emit = defineEmits(['submitted', 'canceled'])

const props = defineProps<{
    dialogVisible: boolean,
    leitorId?: number,
    livroId?: number
}>();

const state = reactive({
    reserva: {} as Reserva,
    livro: null as unknown as Livro,
    dialog: false as boolean,
    listaLivros: [] as Livro[],
    senha: "" as string,
    leitor: null as unknown as Leitor,
    listaLeitores: [] as Leitor[]
})

const constant = {
    notificationStore: useNotificationStore()
}

async function onSubmit(values: any, actions: any) {
    try {
        if (props.leitorId != null)  await ReservaService.reservar(props.leitorId, state.livro.id, state.senha);
        if (props.livroId != null) await  ReservaService.reservar(state.leitor.id, props.livroId, state.senha);
        emit("submitted")
        actions.resetForm();
    }
    catch (err) {
        console.error("Erro ao cadastrar reserva:", err);
        constant.notificationStore.notificar({ mensagem: "Erro ao ao cadastrar reserva!", tipoMensagem: "error", visibilidade: true })
    }
}

watch(
    () => props.dialogVisible,
    (novoDialogVisible) => {
        state.dialog = novoDialogVisible;
    },
);

function cancel() {
    emit("canceled");
}


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

async function buscarLeitores(leitoresSearch: string) {
    if (leitoresSearch != '') {
        try {
            const encodeUriSearch = encodeURIComponent(leitoresSearch);
            const leitoresList = await LeitorService.findAllLeitoresWithCPFFilter(encodeUriSearch);
            state.listaLeitores = leitoresList;
        } catch (error) {
            console.error("Erro ao buscar a lista de leitores", error);
        }
    } else if (state.listaLeitores.length > 0) {
        state.listaLeitores = [];
    }
}

</script>

<template>
    <v-dialog v-model="state.dialog" persistent width="60%">
        <v-card>
            <v-card-title class="mx-auto my-4">
                <span class="text-h5">Cadastrar Reserva</span>
            </v-card-title>
            <v-card-text>
                <Form @submit="onSubmit" @reset="cancel()">
                    <v-container>
                        <v-row >
                            <v-col cols="5" v-if="props.livroId == null">
                                <Field name="livro" v-model="state.livro" v-slot="{ field, errors }">
                                    <v-autocomplete class="mt-6" v-model="state.livro"
                                        v-bind="{field}" :error-messages="errors" :items="state.listaLivros"
                                        item-title="isbn" item-value="id" return-object
                                        label="Filtrar por ISBN do Livro" variant="outlined"
                                        @update:search="buscarLivros"
                                        no-data-text="Digite algum ISBN para buscar os livros"></v-autocomplete>
                                </Field>
                            </v-col>
                            <v-col cols="5" v-if="props.leitorId == null">
                                <Field name="leitor" v-model="state.leitor" v-slot="{ field, errors }">
                                    <v-autocomplete class="mt-6" v-model="state.leitor"
                                        v-bind="{field}" :error-messages="errors" :items="state.listaLeitores"
                                        item-title="cpf" item-value="id" return-object
                                        label="Filtrar por CPF do Leitor" variant="outlined"
                                        @update:search="buscarLeitores"
                                        no-data-text="Digite algum CPF para buscar os leitores"></v-autocomplete>
                                </Field>
                            </v-col>
                            <v-col cols="3" class="mt-6">
                                <Field name="senha" v-model.trim="state.senha" v-slot="{ field, errors }">
                                    <v-text-field type="password" v-bind="field" label="Senha" variant="outlined"
                                        v-model="state.senha" :error-messages="errors">
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
