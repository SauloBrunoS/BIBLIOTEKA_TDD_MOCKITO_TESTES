<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { format, parse } from 'date-fns';

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  errorMessages: {
    type: Array as () => string[],
    default: () => []
  },
  label: {
    type: String,
    default: 'Data'
  }
});

const emit = defineEmits(['update:modelValue']);

const pickedDate = ref<Date | null>(null);
const formattedDate = ref("");
const menu = ref(false);
const selectedMonth = ref("");

const formatDate = (date: Date): string => format(date, "dd/MM/yyyy");
const isoDate = (date: Date): string => format(date, "yyyy-MM-dd");

const updateDate = (newDate: Date | null): void => {
  if (newDate && !isNaN(newDate.getTime())) {
    menu.value = false;
    pickedDate.value = newDate;
    formattedDate.value = formatDate(newDate);
    emit('update:modelValue', isoDate(newDate));

    const month = format(newDate, "MM");
    selectedMonth.value = monthMap[month];
  } else {
    pickedDate.value = null;
    emit('update:modelValue', formattedDate.value);
  }
};

const onDateInput = (event: Event): void => {
  const input = event.target as HTMLInputElement;
  let value = input.value.replace(/\D/g, '');

  if (value.length > 8) value = value.slice(0, 8);
  if (value.length > 2) value = value.replace(/(\d{2})(\d)/, '$1/$2');
  if (value.length > 5) value = value.replace(/(\d{2})\/(\d{2})(\d)/, '$1/$2/$3');

  if (value.length === 0) emit('update:modelValue', '');

  formattedDate.value = value;

  if (formattedDate.value.length === 10) {
    const parsed = parse(formattedDate.value, "dd/MM/yyyy", new Date());
    pickedDate.value = parsed;
    updateDate(parsed);
  }
};

const monthMap: { [key: string]: string } = {
  "01": "JANEIRO",
  "02": "FEVEREIRO",
  "03": "MARÇO",
  "04": "ABRIL",
  "05": "MAIO",
  "06": "JUNHO",
  "07": "JULHO",
  "08": "AGOSTO",
  "09": "SETEMBRO",
  "10": "OUTUBRO",
  "11": "NOVEMBRO",
  "12": "DEZEMBRO",
};

onMounted(() => {
  if (props.modelValue) {
    const parsedDate = new Date(props.modelValue + 'T00:00:00');
    if (!isNaN(parsedDate.getTime())) {
      pickedDate.value = parsedDate;
      formattedDate.value = formatDate(parsedDate);
    }
  }
});

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    const parsedDate = new Date(newValue + 'T00:00:00');
    if (!isNaN(parsedDate.getTime())) {
      pickedDate.value = parsedDate;
      formattedDate.value = formatDate(parsedDate);
    } else {
      pickedDate.value = null;
      formattedDate.value = "";
    }
  } else {
    pickedDate.value = null;
    formattedDate.value = "";
  }
});
</script>

<template>
  <v-menu
    v-model="menu"
    :close-on-content-click="false"
    @click:outside="menu = false"
    transition="scale-transition"
    offset-y
    min-width="auto"
  >
    <template v-slot:activator="{ props }">
      <v-text-field
        v-model="formattedDate"
        :label="label"
        placeholder="Selecione ou digite um dia"
        append-inner-icon="mdi-calendar-range"
        variant="outlined"
        @input="onDateInput"
        v-bind="props"
        :error-messages="errorMessages"
      />
    </template>
    <v-date-picker
      v-model="pickedDate"
      @update:model-value="updateDate"
      locale="pt-br"
      hide-header
    ></v-date-picker>
  </v-menu>
</template>
