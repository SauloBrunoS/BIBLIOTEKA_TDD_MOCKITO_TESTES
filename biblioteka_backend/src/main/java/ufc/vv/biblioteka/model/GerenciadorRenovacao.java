package ufc.vv.biblioteka.model;

import ufc.vv.biblioteka.exception.LimiteExcedidoException;

public class GerenciadorRenovacao {

    public static final int LIMITE_RENOVACOES_SEGUIDAS_DE_UM_MESMO_LIVRO = 3;

    private int getQuantidadeRenovacoesRestantes(int quantidadeRenovacoes) {
        return LIMITE_RENOVACOES_SEGUIDAS_DE_UM_MESMO_LIVRO - quantidadeRenovacoes;
    }

    public void renovar(Emprestimo emprestimo) {
        if (emprestimo == null) {
            throw new IllegalArgumentException("Emprestimo não pode ser nulo");
        }
        if (getQuantidadeRenovacoesRestantes(emprestimo.getQuantidadeRenovacoes()) > 0) {
            emprestimo.renovarLivro();
            emprestimo.marcarDataLimite();
        } else {
            throw new LimiteExcedidoException("Limite de renovações atingido.");
        }
    }
}
