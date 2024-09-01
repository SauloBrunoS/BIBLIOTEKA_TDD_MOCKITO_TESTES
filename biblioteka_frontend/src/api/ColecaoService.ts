import axios from 'axios'
import type { Colecao, Livro } from '@/types'
import httpClient from '@/api/HttpClient'

export default {
  
  async findSearch(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Colecao; order: string }],
    search: string
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/colecoes/buscar`,
      params: {
        search: search,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`
      }
    })
    const { content: colecoes, ...page } = data;
    return { items: colecoes, pagination: page }
  },

  async create(colecao: Colecao): Promise<Colecao> {
    try {
      const { data } = await httpClient.post(`/colecoes`, colecao)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async getById(id: number): Promise<Colecao> {
    try {
      const { data } = await httpClient.get(`/colecoes/${id}`);
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },


  async findAllColecoesWithNomeFilter(search: string): Promise<Colecao[]> {
    try {
      const {data} = await httpClient.get(`colecoes/search/findAllWithFilter?search=${search}`);
      return data._embedded.colecoes;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      console.log(error)

      throw error;
    }
  },


  async findSearchByColecaoId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Livro; order: string }],
    search: string,
    colecaoId: number,
    autorId: number
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/colecoes/${colecaoId}/livros`,
      params: {
        search: search ?? "",
        idAutor: autorId,
        page: pagina,
        autorId: autorId,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: livros, ...page } = data;
    return { items: livros, pagination: page }
  },


  async update(colecao: Colecao, id: number): Promise<Colecao> {
    const { data } = await httpClient({
      method: 'put',
      url: '/colecoes/' + id,
      data: colecao
    })
    return data
  },

  async delete(id: number): Promise<Colecao> {
    const { data } = await httpClient({
      method: 'delete',
      url: `/colecoes/${id}`
    })
    return data
  }
}
