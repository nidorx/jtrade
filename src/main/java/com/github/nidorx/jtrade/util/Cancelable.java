package com.github.nidorx.jtrade.util;

/**
 * FunctionalInterface para ações canceláveis
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@FunctionalInterface
public interface Cancelable {

    public void cancel();
}
