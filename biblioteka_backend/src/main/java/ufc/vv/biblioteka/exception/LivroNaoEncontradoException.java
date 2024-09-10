package ufc.vv.biblioteka.exception;


public class LivroNaoEncontradoException extends RuntimeException {
    public LivroNaoEncontradoException(String message) {
        super(message);
    }
}