<script lang="ts" setup>
import { reactive, watch } from "vue";
import { Form, Field } from 'vee-validate';
import { useNotificationStore } from '@/stores/Notification';
import { Colecao } from '@/types';
import ColecaoService from "@/api/ColecaoService";

const emit = defineEmits(['submitted', 'canceled'])

const props = defineProps<{
    dialogVisible: boolean,
    colecaoId: number
}>();

const state = reactive({
    colecao: {} as Colecao,
    dialog: false as boolean
})

const constant = {
    notificationStore: useNotificationStore()
}

function modoEdicao() {
    return !!props.colecaoId;
}

async function loadColecao() {
    if (props.colecaoId == null) state.colecao = {} as Colecao;
    else {
        try {
            const colecao = await ColecaoService.getById(props.colecaoId);
            state.colecao = colecao;
        } catch (error) {
            console.error("Erro ao carregar detalhes da coleção: ", error);
        }
    }
}

async function onSubmit(values: any, actions: any) {
    if (modoEdicao()) {
        try {
            await ColecaoService.update(values, props.colecaoId);
        }
        catch (err) {
            console.error("Erro ao alterar coleção:", err);
            constant.notificationStore.notificar({ mensagem: "Erro ao alterar coleção!", tipoMensagem: "error", visibilidade: true })
        }
    } else {
        try {
            await ColecaoService.create(values);
        } catch (err) {
            console.error("Erro ao cadastrar nova coleção:", err);
            constant.notificationStore.notificar({ mensagem: "Erro ao cadastrar nova coleção!", tipoMensagem: "error", visibilidade: true })
        }
    }
    emit("submitted")
    actions.resetForm();
}

watch(
    () => props.dialogVisible,
    (novoDialogVisible) => {
        state.dialog = novoDialogVisible;
        if (novoDialogVisible) loadColecao();
    },
);

function cancel() {
    emit("canceled");
}

</script>

<template>
    <v-dialog v-model="state.dialog" persistent width="60%">
        <v-card>
            <v-card-title class="mx-auto my-4">
                <span class="text-h5">{{ modoEdicao() ? 'Editar' : 'Cadastrar' }} Coleção</span>
            </v-card-title>
            <v-card-text>
                <v-container>
                    <Form @submit="onSubmit" @reset="cancel()">
                        <v-row>
                            <Field name="nome" v-model.trim="state.colecao.nome" v-slot="{ field, errors }">
                                <v-col cols="12">
                                    <v-text-field v-bind="field" label="Nome" variant="outlined"
                                        v-model="state.colecao.nome" :error-messages="errors">
                                    </v-text-field>
                                </v-col>
                            </Field>
                        </v-row>
                        <v-row>
                            <v-col cols="12">
                                <Field name="descricao" v-model.trim="state.colecao.descricao"
                                    v-slot="{ field, errors }">
                                    <v-textarea label="Descrição" variant="outlined"
                                        v-model="state.colecao.descricao" v-bind="field" :error-messages="errors" />
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
                    </Form>
                </v-container>
            </v-card-text>
        </v-card>
    </v-dialog>
</template>
