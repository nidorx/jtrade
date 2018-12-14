package com.github.nidorx.jtrade.util.function;

import java.util.Objects;

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

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this operation followed by the {@code after}
     * operation
     * @throws NullPointerException if {@code after} is null
     */
    default TrheeConsumer<T, U, V> andThen(TrheeConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }

}
