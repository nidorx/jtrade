package com.github.nidorx.jtrade.broker.impl.metatrader.model;

/**
 * Lista dos comandos conhecidos pleo EA
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum Topic {

    TICK(1),
    RATES(2)
    ;

    public static Topic getByCode(int code) {
        for (Topic value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public final int code;

    private Topic(int code) {
        this.code = code;
    }

}
