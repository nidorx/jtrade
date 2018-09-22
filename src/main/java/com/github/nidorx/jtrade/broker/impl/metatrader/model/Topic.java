package com.github.nidorx.jtrade.broker.impl.metatrader.model;

import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.Tick;
import com.github.nidorx.jtrade.util.SDParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Lista dos comandos conhecidos pleo EA
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum Topic {

    TICK(1, (String message) -> {
        return new Tick(message);
    }),
    RATES(2, (String message) -> {
        return new Rate(message);
    }),
    /**
     * Tópico com informação sobre todos os servers da mesma conta para o EA
     */
    SERVES(3, (String message) -> {
        final List<Integer> servers = new ArrayList<>();
        SDParser p = new SDParser(message, '_');
        while (p.hasMore()) {
            servers.add(p.popInt());
        }
        return servers;
    });

    public static Topic getByCode(int code) {
        for (Topic value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public final int code;

    private final Function<String, Object> decoder;

    private Topic(int code, Function<String, Object> decoder) {
        this.code = code;
        this.decoder = decoder;
    }

    public <T> T decode(String message) {
        return (T) this.decoder.apply(message);
    }
}
