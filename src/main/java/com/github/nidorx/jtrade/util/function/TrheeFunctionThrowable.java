package com.github.nidorx.jtrade.util.function;

@FunctionalInterface
public interface TrheeFunctionThrowable<T, U, V, R> {

    R apply(T t, U u, V v) throws Exception;
}
