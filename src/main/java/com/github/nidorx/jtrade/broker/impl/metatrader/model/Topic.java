package com.github.nidorx.jtrade.broker.impl.metatrader.model;

import com.github.nidorx.jtrade.core.Account;
import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.Tick;
import com.github.nidorx.jtrade.util.StringDelimitedParser;
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
    SERVERS(3, (String message) -> {
        final List<Integer> servers = new ArrayList<>();
        StringDelimitedParser p = new StringDelimitedParser(message, '_');
        while (p.hasMore()) {
            servers.add(p.popInt());
        }
        return servers;
    }),
    /**
     * Informações sobre a atualização da conta
     */
    ACCOUNT(4, (String message) -> {
        return new Account(message);
    }),
    POSITION(5, (String message) -> {
        // "<SYMBOL>|<POSITION><POSITION><POSITION>|<ORDER><ORDER><ORDER>|<DEAL><DEAL><DEAL>"
        // POSITION: "TIME_MSC IDENTIFIER TYPE PRICE_OPEN VOLUME SL TP"   
        // ORDER: "TIME_MSC TICKET POSITION TYPE STATE FILLING PRICE VOLUME SL TP STOPLIMIT"
        // DEAL: "TIME_MSC TICKET ORDER POSITION TYPE ENTRY PRICE VOLUME COMMISSION SWAP PROFIT"

        return message;
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
