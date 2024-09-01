import axios from 'axios'
import type { Emprestimo } from '@/types'
import httpClient from '@/api/HttpClient'

export default {
  async emprestar(leitorId: number, livroId: number, senha: string): Promise<Emprestimo> {
    try {
      const { data } = await httpClient({
        method: 'post',
        url: `/emprestimos/emprestar`,
        params: {
          leitorId: leitorId,
          livroId: livroId,
          senha: senha
        }
      })
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error)
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async devolver(emprestimoId: number, senha: string): Promise<Emprestimo> {
    try {
      const { data } = await httpClient({
        method: 'post',
        url: `/emprestimos/devolver`,
        params: {
          emprestimoId: emprestimoId,
          senha: senha,
        }
      })
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error)
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async findSearchEmprestimosByLeitorId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Emprestimo; order: string }],
    search: string,
    leitorId: number,
    livroId: number,
    devolvido: boolean
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/leitores/${leitorId}/emprestimos`,
      params: {
        search: search ?? "",
        livroId: livroId,
        devolvido: devolvido,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: emprestimos, ...page } = data;
    return { items: emprestimos, pagination: page }
  },

  async findSearchEmprestimosByLivroId(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Emprestimo; order: string }],
    search: string,
    livroId: number,
    leitorId: number,
    devolvido: boolean
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/livros/${livroId}/emprestimos`,
      params: {
        search: search ?? "",
        leitorId: leitorId,
        devolvido: devolvido,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`,
      }
    })
    const { content: emprestimos, ...page } = data;
    return { items: emprestimos, pagination: page }
  },


  async renovar(emprestimoId: number, senha: string): Promise<Emprestimo> {
    try {
      const { data } = await httpClient({
        method: 'post',
        url: `/emprestimos/renovar`,
        params: {
          emprestimoId: emprestimoId,
          senha: senha,
        }
      })
      return data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error)
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },


  async getById(id: number): Promise<Emprestimo> {
    try {
      const { data } = await httpClient.get(`/emprestimos/${id}`)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data)
      }
      throw error
    }
  }
}
