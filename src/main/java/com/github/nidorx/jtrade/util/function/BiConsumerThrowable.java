package com.github.nidorx.jtrade.util.function;

/**
 * Representa uma operação que recebe dois argumentos de entrada e não possui retorno e pode lançar exceções.
 *
 * <p>
 * Essa é uma <strong>interface funcional</strong> cujo método funcional é {@link #accept(Object, Object)}.
 *
 * @param <T> O tipo do primeiro argumento para a operação
 * @param <U> O tipo do segundo argumento para a operação
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface BiConsumerThrowable<T, U> {

    /**
     * Execute esta operação com os argumentos informados
     *
     * @param t O primeiro argumento de entrada
     * @param u O segundo argumento de entrada
     * @throws java.lang.Exception
     */
    void accept(T t, U u) throws Exception;

}
