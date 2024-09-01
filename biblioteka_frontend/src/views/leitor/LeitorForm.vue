<script lang="ts" setup>
import { computed, reactive, watch } from "vue";
import { Form, Field } from 'vee-validate';
import { useNotificationStore } from '@/stores/Notification';
import { Leitor, Usuario } from '@/types';
import LeitorService from "@/api/LeitorService";
const emit = defineEmits(['submitted', 'canceled'])

const props = defineProps<{
    dialogVisible: boolean,
    leitorId: number
}>();

const state = reactive({
    leitor: {} as Leitor,
    dialog: false as boolean,
    senha: "" as string,
    confirmacaoSenha: "" as string
})

const constant = {
    notificationStore: useNotificationStore()
}

function modoEdicao() {
    return !!props.leitorId;
}

async function loadLeitor() {
    if (props.leitorId == null) state.leitor = {} as Leitor;
    else {
        try {
            const leitor = await LeitorService.getById(props.leitorId);
            state.leitor = leitor;
        } catch (error) {
            console.error("Erro ao carregar detalhes do leitor: ", error);
        }
    }
}

async function onSubmit(values: any, actions: any) {
    if (state.senha !== state.confirmacaoSenha) {
        constant.notificationStore.notificar({ mensagem: "Senhas não podem ser diferentes!", tipoMensagem: "error", visibilidade: true });
        return;
    }
    if (modoEdicao()) {
        try {
            delete values.confirmacaoSenha;
            if(values.senha != "")values.usuario = { id: state.leitor.usuario.id, email: values.email, senha: values.senha }
            else values.usuario = { id: state.leitor.usuario.id, email: values.email}
            delete values.email;
            delete values.senha;
            console.log(values)
            await LeitorService.update(values, props.leitorId);
        }
        catch (err) {
            console.error("Erro ao alterar leitor:", err);
            constant.notificationStore.notificar({ mensagem: "Erro ao alterar leitor!", tipoMensagem: "error", visibilidade: true })
        }
    } else {
        try {
            delete values.confirmacaoSenha;
            values.usuario = { id: state.leitor.usuario.id, email: values.email, senha: values.senha }
            delete values.email;
            delete values.senha;
            await LeitorService.create(values);
        } catch (err) {
            console.error("Erro ao cadastrar novo leitor:", err);
            constant.notificationStore.notificar({ mensagem: "Erro ao cadastrar novo leitor!", tipoMensagem: "error", visibilidade: true })
        }
    }
    emit("submitted")
    actions.resetForm();
}

watch(
    () => props.dialogVisible,
    (novoDialogVisible) => {
        state.dialog = novoDialogVisible;
        if (novoDialogVisible) loadLeitor();
    },
);

function cancel() {
    emit("canceled");
}

const usuarioEmail = computed({
    get() {
        return state.leitor.usuario?.email || '';
    },
    set(value) {
        if (state.leitor.usuario) {
            state.leitor.usuario.email = value;
        } else {
            state.leitor.usuario = { email: value } as Usuario;
        }
    }
});
</script>

<template>
    <v-dialog v-model="state.dialog" persistent width="60%">
        <v-card>
            <v-card-title class="mx-auto my-4">
                <span class="text-h5">{{ modoEdicao() ? 'Editar' : 'Cadastrar' }} Leitor</span>
            </v-card-title>
            <v-card-text>
                <Form @submit="onSubmit" @reset="cancel()">
                    <v-container>
                        <v-row>
                            <v-col cols="5">
                                <Field name="nomeCompleto" v-model.trim="state.leitor.nomeCompleto"
                                    v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" label="Nome completo" variant="outlined"
                                        v-model="state.leitor.nomeCompleto" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="3">
                                <Field name="cpf" v-model.trim="state.leitor.cpf" v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" label="CPF" variant="outlined"
                                        v-model="state.leitor.cpf" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="3">
                                <Field name="telefone" v-model.trim="state.leitor.telefone" v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" label="Telefone" variant="outlined"
                                        v-model="state.leitor.telefone" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="5">
                                <Field name="email" v-model.trim="usuarioEmail" v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" label="E-mail" variant="outlined"
                                        v-model="usuarioEmail" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="3">
                                <Field name="senha" v-if="!modoEdicao()" v-model.trim="state.senha" v-slot="{ field, errors }">
                                    <v-text-field type="password" v-bind="field" label="Senha" variant="outlined"
                                        v-model="state.senha" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="3">
                                <Field name="confirmacaoSenha" v-if="!modoEdicao()" v-model.trim="state.confirmacaoSenha"
                                    v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" type="password" label="Confirmação de Senha"
                                        variant="outlined" v-model="state.confirmacaoSenha" :error-messages="errors">
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
