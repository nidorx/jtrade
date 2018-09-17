package com.github.nidorx.jtrade.broker.impl.metatrader;

/**
 * Lista dos comandos conhecidos pleo EA
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum Command {

    XPTO("xpto", 1);

    public final String name;

    public final int code;

    private Command(String name, int code) {
        this.name = name;
        this.code = code;
    }

}
