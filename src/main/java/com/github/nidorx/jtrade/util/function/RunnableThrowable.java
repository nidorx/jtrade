package com.github.nidorx.jtrade.util.function;

/**
 * @see java.lang.Runnable
 * @author Alex Rodin {@literal <contato@alexrodin.info>}
 */
@FunctionalInterface
public interface RunnableThrowable {

    void apply() throws Exception;
}
