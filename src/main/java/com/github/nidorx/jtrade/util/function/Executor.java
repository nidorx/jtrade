package com.github.nidorx.jtrade.util.function;

import java.util.Objects;

/**
 * Representa uma operação que não recebe argumentos de entrada e não possui retorno.
 *
 */
@FunctionalInterface
public interface Executor {

    /**
     * Execute esta operação com os argumentos informados
     *
     */
    void exec();

    /**
     * Returns a composed {@code Executor} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Executor} that performs in sequence this operation followed by the {@code after}
     * operation
     * @throws NullPointerException if {@code after} is null
     */
    default Executor andThen(Executor after) {
        Objects.requireNonNull(after);
        return () -> {
            exec();
            after.exec();
        };
    }

}
