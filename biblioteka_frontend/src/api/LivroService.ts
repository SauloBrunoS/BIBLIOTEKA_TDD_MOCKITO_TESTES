import axios from 'axios'
import type { Livro } from '@/types'
import httpClient from '@/api/HttpClient'

export default {
  
  async findSearch(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Livro; order: string }],
    search: string,
    colecaoId: number,
    autorId: number
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/livros/buscar`,
      params: {
        search: search ?? "",
        colecaoId: colecaoId,
        autorId: autorId,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: livros, ...page } = data;
    return { items: livros, pagination: page }
  },

  async create(livro: Livro): Promise<Livro> {
    try {
      const { data } = await httpClient.post(`/livros`, livro)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async findAllLivrosWithISBNFilter(search: string): Promise<Livro[]> {
    try {
      const {data} = await httpClient.get(`livros/search/findAllWithISBNFilter?search=${search}`);
      return data._embedded.livros;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },

  async getById(id: number): Promise<Livro> {
    try {
      const { data } = await httpClient.get(`/livros/${id}`);
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data);
      }
      throw error;
    }
  },

  async update(livro: Livro, id: number): Promise<Livro> {
    const { data } = await httpClient({
      method: 'put',
      url: '/livros/' + id,
      data: livro
    })
    return data
  },

  async delete(id: number): Promise<Livro> {
    const { data } = await httpClient({
      method: 'delete',
      url: `/livros/${id}`
    })
    return data
  }

}
