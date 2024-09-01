import axios from 'axios'
import type { Autor, Livro } from '@/types'
import httpClient from '@/api/HttpClient'
import { Nacionalidade } from '@/types/enum';

export default {
  
  async findSearch(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Autor; order: string }],
    search: string,
    nacionalidade: keyof typeof Nacionalidade
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/autores/buscar`,
      params: {
        search: search ?? "",
        nacionalidade: nacionalidade,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: autores, ...page } = data;
    return { items: autores, pagination: page }
  },

  async create(autor: Autor): Promise<Autor> {
    try {
      const { data } = await httpClient.post(`/autores`, autor)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async getById(id: number): Promise<Autor> {
    try {
      const { data } = await httpClient.get(`/autores/${id}`);
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },


  async findSearchLivrosByAutorId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Livro; order: string }],
    search: string,
    autorId: number,
    colecaoId: number
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/autores/${autorId}/livros`,
      params: {
        search: search ?? "",
        idColecao: colecaoId,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: livros, ...page } = data;
    return { items: livros, pagination: page }
  },

  async findAllAutoresWithNomeFilter(search: string): Promise<Autor[]> {
    try {
      const {data} = await httpClient.get(`autores/search/findAllWithFilter?search=${search}`);
      return data._embedded.autores;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },

  async update(autor: Autor, id: number): Promise<Autor> {
    const { data } = await httpClient({
      method: 'put',
      url: '/autores/' + id,
      data: autor
    })
    return data
  },

  async delete(id: number): Promise<Autor> {
    const { data } = await httpClient({
      method: 'delete',
      url: `/autores/${id}`
    })
    return data
  }
}
