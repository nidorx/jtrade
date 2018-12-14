package com.github.nidorx.jtrade.util.function;

/**
 * Representa uma função que aceita um argumento e produz um resultado e lança exceções.
 *
 * <p>
 * Essa é uma <strong>interface funcional</strong> cujo método funcional é {@link #apply(Object)}.
 *
 * @param <T> O tipo de entrada para a função
 * @param <R> O tipo de resultado da função
 *
 * @see java.util.function.Function
 * @author Alex Rodin {@literal <contato@alexrodin.info>}
 */
@FunctionalInterface
public interface FunctionThrowable<T, R> {

    /**
     * Aplica esta função ao argumento fornecido.
     *
     * @param t O argumento da função
     * @return O resultado da função
     * @throws Exception
     */
    R apply(T t) throws Exception;
}
