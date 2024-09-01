import axios from 'axios'
import type { Leitor } from '@/types'
import httpClient from '@/api/HttpClient'
import { StatusReserva } from '@/types/enum';

export default {
  
  async findSearch(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Leitor; order: string }],
    search: string,
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/leitores/buscar`,
      params: {
        search: search ?? "",
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: leitores, ...page } = data;
    return { items: leitores, pagination: page }
  },

  async create (leitor: Leitor): Promise<Leitor> {
    try {
      const { data } = await httpClient.post(`/leitores`, leitor)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async findAllLeitoresWithCPFFilter(search: string): Promise<Leitor[]> {
    try {
      const {data} = await httpClient.get(`leitores/search/findAllWithCPFFilter?search=${search}`);
      return data._embedded.leitores;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },

  async getById(id: number): Promise<Leitor> {
    try {
      const { data } = await httpClient.get(`/leitores/${id}`);
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },

  async findSearchReservasByLeitorId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Leitor; order: string }],
    search: string,
    leitorId: number,
    statusReserva: keyof typeof StatusReserva
    ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/leitores/${leitorId}/reservas`,
      params: {
        search: search ?? "",
        status: statusReserva,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: reservas, ...page } = data;
    return { items: reservas, pagination: page }
  },

  async update(leitor: Leitor, id: number): Promise<Leitor> {
    const { data } = await httpClient({
      method: 'put',
      url: '/leitores/' + id,
      data: leitor
    })
    return data
  },

  async delete(id: number): Promise<Leitor> {
    const { data } = await httpClient({
      method: 'delete',
      url: `/leitores/${id}`
    })
    return data
  }
}
