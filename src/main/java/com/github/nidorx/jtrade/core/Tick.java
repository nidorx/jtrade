package com.github.nidorx.jtrade.core;

import com.github.nidorx.jtrade.util.StringDelimitedParser;
import java.time.Instant;

/**
 * Representa um único Tick
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class Tick {

    public final String symbol;

    public final Instant time;

    public final double bid;

    public final double ask;

    public final double last;

    public final double volume;

    /**
     * No formato "SYMBOL TIME_MILIS BID ASK LAST VOLUME"
     *
     * Ex. "EURUSD 1537429422098 0.88823000 0.88835000 0.00000000 0"
     *
     * @param data
     */
    public Tick(String data) {
        StringDelimitedParser p = new StringDelimitedParser(data, ' ');
        this.symbol = p.pop();
        this.time = Instant.ofEpochMilli(p.popLong());
        this.bid = p.popDouble();
        this.ask = p.popDouble();
        this.last = p.popDouble();
        this.volume = p.popDouble();
    }

    public Tick(String symbol, Instant time, double bid, double ask, double last, double volume) {
        this.symbol = symbol;
        this.time = time;
        this.bid = bid;
        this.ask = ask;
        this.last = last;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return " " + time + " " + symbol + " ask=" + ask + " bid=" + bid + " last=" + last + " volume=" + volume;
    }
}
