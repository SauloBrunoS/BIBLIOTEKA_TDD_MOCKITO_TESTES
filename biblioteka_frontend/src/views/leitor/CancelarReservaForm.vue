<script lang="ts" setup>
import { reactive, watch } from "vue";
import { Form, Field } from 'vee-validate';
import { useNotificationStore } from '@/stores/Notification';
import EmprestimoService from "@/api/EmprestimoService";
import ReservaService from "@/api/ReservaService";
const emit = defineEmits(['submitted', 'canceled'])

const props = defineProps<{
    dialogVisible: boolean,
    reservaId: number
}>();

const state = reactive({
    senha: "" as string,
    dialog: false as boolean
})

const constant = {
    notificationStore: useNotificationStore()
}

async function onSubmit(values: any, actions: any) {
    try {
        await ReservaService.cancelar(props.reservaId, state.senha);
        emit("submitted")
        actions.resetForm();
    }
    catch (err) {
        console.error("Erro ao cancelar reserva:", err);
        constant.notificationStore.notificar({ mensagem: "Erro ao ao cancelar reserva!", tipoMensagem: "error", visibilidade: true })
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

</script>

<template>
    <v-dialog v-model="state.dialog" persistent width="60%">
        <v-card>
            <v-card-title class="mx-auto my-4">
                <span class="text-h5">Cancelar Reserva</span>
            </v-card-title>
            <v-card-text>
                <Form @submit="onSubmit" @reset="cancel()">
                    <v-container>
                        <v-row >
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
