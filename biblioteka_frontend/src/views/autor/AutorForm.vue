<script lang="ts" setup>
import { onMounted, reactive, watch } from "vue";
import { Form, Field } from 'vee-validate';
import { Autor } from '@/types';
import AutorService from "@/api/AutorService";
import { Nacionalidade } from "@/types/enum";
import dayjs from 'dayjs';
import DatePicker from '@/components/DatePicker.vue';
const emit = defineEmits(['submitted', 'canceled'])

const props = defineProps<{
    dialogVisible: boolean,
    autorId: number
}>();

const state = reactive({
    autor: {} as Autor,
    dialog: false as boolean,
    showDatePicker: false as boolean,
})

function modoEdicao() {
    return !!props.autorId;
}

async function loadAutor() {
    if (props.autorId == null) state.autor = {} as Autor;
    else {
        try {
            const autor = await AutorService.getById(props.autorId);
            state.autor = autor;
        } catch (error) {
            console.error("Erro ao carregar detalhes do autor: ", error);
        }
    }
}

async function onSubmit(values: any, actions: any) {
    if (modoEdicao()) {
        try {
            values.dataNascimento = dayjs(state.autor.dataNascimento).format("YYYY-MM-DD");
            await AutorService.update(values, props.autorId);
        }
        catch (err) {
            console.error("Erro ao alterar autor:", err);
        }
    } else {
        try {
            if (values.dataNascimento != null) values.dataNascimento = dayjs(state.autor.dataNascimento).format("YYYY-MM-DD");
            console.log(values)
            await AutorService.create(values);
        } catch (err) {
            console.error("Erro ao cadastrar novo autor:", err);
        }
    }
    emit("submitted")
    actions.resetForm();
}

watch(
    () => props.dialogVisible,
    (novoDialogVisible) => {
        state.dialog = novoDialogVisible;
        if (novoDialogVisible) {
            loadAutor();
            state.autor.dataNascimento = null as unknown as string;
        }
    },
);

function cancel() {
    emit("canceled");
}

function createOptions<T>(enumObject: T) {
    return Object.keys(enumObject).map(key => ({
        text: enumObject[key as keyof T],
        value: key == "NENHUM" ? null : key
    }));
}

const nacionalidadeOptions = createOptions(Nacionalidade)

</script>

<template>
    <v-dialog v-model="state.dialog" persistent width="60%">
        <v-card>
            <v-card-title class="mx-auto my-4">
                <span class="text-h5">{{ modoEdicao() ? 'Editar' : 'Cadastrar' }} Autor</span>
            </v-card-title>
            <v-card-text>
                <Form @submit="onSubmit" @reset="cancel()">
                    <v-container>
                        <v-row>
                            <v-col cols="5">
                                <Field name="nomeCompleto" v-model.trim="state.autor.nomeCompleto"
                                    v-slot="{ field, errors }">
                                    <v-text-field v-bind="field" label="Nome completo" variant="outlined"
                                        v-model="state.autor.nomeCompleto" :error-messages="errors">
                                    </v-text-field>
                                </Field>
                            </v-col>
                            <v-col cols="3">
                                <Field name="nacionalidade" v-model="state.autor.nacionalidade"
                                    v-slot="{ field, errors }">
                                    <v-autocomplete v-bind="{ field }" v-model="state.autor.nacionalidade"
                                        :error-messages="errors" :items="nacionalidadeOptions" item-title="text"
                                        item-value="value" label="Nacionalidade" variant="outlined"></v-autocomplete>
                                </Field>
                            </v-col>
                            <v-col cols="4">
                                <Field name="dataNascimento" v-model="state.autor.dataNascimento"
                                    v-slot="{ field, errors }">
                                    <date-picker v-model="state.autor.dataNascimento" v-bind="{ field }"
                                        :error-messages="errors" label="Data de Nascimento"
                                        @update:modelValue="(newValue) => state.autor.dataNascimento = newValue" />
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
