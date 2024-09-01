import { useNotificationStore } from '@/stores/Notification'
import axios from 'axios'
import { JsogService } from 'jsog-typescript'

const jsog = new JsogService()

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000
})

httpClient.interceptors.response.use(
  (response) => {
    const notificationStore = useNotificationStore()

    if (
      response.config.method !== 'get' &&
      (response.status === 200 || response.status === 201 || response.status === 204)
    ) {
      notificationStore.notificar({
        mensagem: 'Operação realizada com sucesso!',
        tipoMensagem: 'success',
        visibilidade: true
      })
    }

    response.data = jsog.deserialize(response.data)

    return response
  },
  (error) => {
    console.log(error)
    const notificationStore = useNotificationStore()
    let complementoErro = ''
    complementoErro = error.response.data?.userMessage

    notificationStore.notificar({
      mensagem: `Erro: Ação não concluída! ${complementoErro}`,
      tipoMensagem: 'error',
      visibilidade: true
    })
    return Promise.reject(error)
  }
)

export default httpClient
