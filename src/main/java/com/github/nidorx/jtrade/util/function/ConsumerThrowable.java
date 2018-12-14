package com.github.nidorx.jtrade.util.function;

/**
 * Representa uma operação que recebe um argumento de entrada e não possui retorno e pode lançar exceções.
 *
 * <p>
 * Essa é uma <strong>interface funcional</strong> cujo método funcional é {@link #accept(Object)}.
 *
 * @param <T> O tipo do argumento para a operação
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface ConsumerThrowable<T> {

    /**
     * Execute esta operação com o argumento informado
     *
     * @param t O argumento de entrada
     * @throws java.lang.Exception
     */
    void accept(T t) throws Exception;

}
