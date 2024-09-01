// Plugins
import { loadFonts } from './webfontloader'
import vuetify from './vuetify'
import pinia from '@/stores'

// Types
import type { App } from 'vue'

import VueDatePicker from '@vuepic/vue-datepicker';
import '@vuepic/vue-datepicker/dist/main.css'


export function registerPlugins (app: App) {
  loadFonts()
  app
    .use(vuetify)
    .use(pinia)
    .component('VueDatePicker', VueDatePicker);
}
