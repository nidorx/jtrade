package com.github.nidorx.jtrade.util.function;

@FunctionalInterface
public interface TrheeFunction<T, U, V, R> {

    R apply(T t, U u, V v);
}
