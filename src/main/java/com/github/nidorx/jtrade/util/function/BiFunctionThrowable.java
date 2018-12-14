package com.github.nidorx.jtrade.util.function;

@FunctionalInterface
public interface BiFunctionThrowable<T, U, R> {

    R apply(T t, U u) throws Exception;
}
