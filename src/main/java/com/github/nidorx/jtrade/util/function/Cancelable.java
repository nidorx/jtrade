package com.github.nidorx.jtrade.util.function;

/**
 * FunctionalInterface para ações canceláveis
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@FunctionalInterface
public interface Cancelable {

    public void cancel();
}
