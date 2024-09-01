// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'
import { pt } from 'vuetify/locale'

// Composables
import { createVuetify } from 'vuetify'
import {
  VApp,
  VAppBar,
  VAppBarNavIcon,
  VToolbarTitle,
  VToolbar,
  VTabs,
  VTab,
  VCard,
  VCardText,
  VContainer,
  VRow,
  VCol,
  VMain,
  VIcon,
  VToolbarItems,
  VTextField,
  VSnackbar
} from 'vuetify/components'
import { VDataTable, VDataTableServer } from 'vuetify/components/VDataTable'
import { VBtn } from 'vuetify/lib/components/index.mjs'

const myCustomLightTheme = {
  dark: false,
  colors: {
    background: '#FFFFFF',
    surface: '#EFEFEF',
    primary: '#AFE8C2',
    secondary: '#D8F0E0',
    error: '#C61A1A',
    info: '#2196F3',
    success: '#199334',
    warning: '#FB8C00'
  }
}

export default createVuetify({
  components: {
    VApp,
    VAppBar,
    VAppBarNavIcon,
    VToolbarTitle,
    VToolbar,
    VToolbarItems,
    VTabs,
    VTab,
    VCard,
    VCardText,
    VContainer,
    VRow,
    VSnackbar,
    VCol,
    VTextField,
    VDataTable,
    VDataTableServer,
    VBtn,
    VMain,
    VIcon
  },
  theme: {
    defaultTheme: 'myCustomLightTheme',
    themes: {
      myCustomLightTheme
    }
  },
  locale: {
    locale: 'pt',
    fallback: 'pt',
    messages: { pt }
  },
  aliases: {
    VBtnEdit: VBtn,
    VBtnDelete: VBtn,
    VBtnDetails: VBtn,
    VBtnItens: VBtn,
    VBtnDownload: VBtn
  },
  defaults: {
    VBtnEdit: {
      color: 'info',
      variant: 'text',
      icon: 'mdi-pencil'
    },
    VBtnDelete: {
      color: 'error',
      variant: 'text',
      icon: 'mdi-delete'
    },
    VBtnDetails: {
      color: '#609B74',
      variant: 'text',
      icon: 'mdi-text-search'
    },
    VBtnDownload: {
      color: 'blue-darken-2',
      variant: 'text',
      icon: 'mdi-download'
    },
    VBtnItens: {
      color: '#609B74',
      variant: 'text',
      icon: 'mdi-format-list-bulleted'
    }
  }
})