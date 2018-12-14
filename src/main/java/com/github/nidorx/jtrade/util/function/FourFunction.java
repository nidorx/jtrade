package com.github.nidorx.jtrade.util.function;

@FunctionalInterface
public interface FourFunction<T, U, V, W, R> {

    R apply(T t, U u, V v, W w) throws Exception;
}
