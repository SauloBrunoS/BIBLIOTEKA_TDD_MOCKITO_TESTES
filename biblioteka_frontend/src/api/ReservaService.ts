import axios from 'axios'
import type { Reserva } from '@/types'
import httpClient from '@/api/HttpClient'
import { StatusReserva } from '@/types/enum'

export default {
  async reservar(leitorId: number, livroId: number, senha: string): Promise<Reserva> {
    try {
      const { data } = await httpClient({
        method: 'post',
        url: `/reservas/reservar`,
        params: {
          leitorId: leitorId,
          livroId: livroId,
          senha: senha
        }
      })
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error)
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async cancelar(reservaId: number, senha: string): Promise<Reserva> {
    try {
      const { data } = await httpClient({
        method: 'post',
        url: `/reservas/cancelar`,
        params: {
          reservaId: reservaId,
          senha: senha
        }
      })
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error)
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async findSearchReservasByLeitorId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Reserva; order: string }],
    search: string,
    leitorId: number,
    livroId: number,
    status: keyof typeof StatusReserva
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/leitores/${leitorId}/reservas`,
      params: {
        search: search ?? '',
        livroId: livroId,
        status: status,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`
      }
    })
    const { content: reservas, ...page } = data
    return { items: reservas, pagination: page }
  },

  async findSearchReservasByLivroId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Reserva; order: string }],
    search: string,
    livroId: number,
    leitorId: number,
    status: keyof typeof StatusReserva
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/livros/${livroId}/reservas`,
      params: {
        search: search ?? '',
        leitorId: leitorId,
        status: status,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`
      }
    })
    const { content: reservas, ...page } = data
    return { items: reservas, pagination: page }
  },

  async getById(id: number): Promise<Reserva> {
    try {
      const { data } = await httpClient.get(`/reservas/${id}`)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data)
      }
      throw error
    }
  }
}
