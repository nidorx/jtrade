package com.github.nidorx.jtrade.util;

/**
 * Representa uma operação que recebe três argumentos de entrada e não possui retorno e pode lançar exceções.
 *
 * <p>
 * Essa é uma <strong>interface funcional</strong> cujo método funcional é {@link #accept(Object, Object, Object)}.
 *
 * @param <T> O tipo do primeiro argumento para a operação
 * @param <U> O tipo do segundo argumento para a operação
 * @param <V> O tipo do terceiro argumento para a operação
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface TrheeConsumer<T, U, V> {

    /**
     * Execute esta operação com os argumentos informados
     *
     * @param t O primeiro argumento de entrada
     * @param u O segundo argumento de entrada
     * @param v O terceiro argumento de entrada
     */
    void accept(T t, U u, V v);

}
