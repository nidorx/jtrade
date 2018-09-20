package com.github.nidorx.jtrade;

import com.github.nidorx.jtrade.util.SDParser;
import java.time.Instant;

/**
 * Representa um único Tick
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class Tick {

    public final Instant time;

    public final double bid;

    public final double ask;

    public final double last;

    public final double volume;

    /**
     * No formato "TIME BID ASK LAST VOLUME"
     *
     * Ex. "1537429422098 0.88823000 0.88835000 0.00000000 0"
     *
     * @param data
     */
    public Tick(String data) {
        SDParser p = new SDParser(data, ' ');
        this.time = Instant.ofEpochMilli(p.popLong());
        this.bid = p.popDouble();
        this.ask = p.popDouble();
        this.last = p.popDouble();
        this.volume = p.popDouble();
    }

    public Tick(Instant time, double bid, double ask, double last, double volume) {
        this.time = time;
        this.bid = bid;
        this.ask = ask;
        this.last = last;
        this.volume = volume;
    }
}
