import { Nacionalidade, StatusReserva } from "./enum"

export interface Colecao {
  id: number,
  nome: string,
  descricao: string
}

export interface Autor {
  id: number,
  nomeCompleto: string,
  dataNascimento: string,
  nacionalidade: keyof typeof Nacionalidade,
}

export interface Livro {
  id: number,
  titulo: string,
  isbn: string
  dataPublicacao: string,
  qtdPaginas: number,
  numeroCopiasTotais:number,
  numeroCopiasDisponiveis:number,
  autores: Autor[],
  colecoes: Colecao[]
}

export interface Leitor {
  id: number,
  nomeCompleto: string,
  cpf: string,
  telefone: string,
  usuario: Usuario
}

export interface Usuario {
  id: number,
  email: string
}

export interface Emprestimo {
  id: number;
  leitor: Leitor
  livro: Livro
  dataEmprestimo: string;
  dataLimite: string; 
  dataDevolucao?: string; 
  quantidadeRenovacoes: number;
  valorBase: number;
  multa: number;
  valorTotal: number;
  devolvido: boolean;
  reserva: Reserva;
};

export interface Reserva {
  id: number;
  leitor: Leitor,
  livro: Livro,
  dataCadastro: string; 
  dataLimite: string; 
  status: keyof typeof StatusReserva; 
  emprestimo: Emprestimo
};

export interface InfoDataTableServer {
  id?: number
  page: number
  itemsPerPage: number
  sortBy: [{ key: string; order: string }]
  search: string
}

export interface ApplicationError {
  name: string
  message: string
  details?: string[]
}

export interface Notificacao {
  mensagem: string
  tipoMensagem: string
  visibilidade: boolean
}
