package com.github.nidorx.jtrade;

import com.github.nidorx.jtrade.util.SDParser;
import java.util.HashMap;
import java.util.Map;

/**
 * Armazena as informações sobre Preço e volume de um ativo em um determinado periodo
 *
 * @TODO: Volume
 *
 * @author Alex
 */
public class Rate {

    public final String symbol;

    public final long time;

    public final double open;

    public final double high;

    public final double low;

    public final double close;

    public final long volumeTick;

    public final long volumeReal;

    public final int spread;

    public final TimeFrame timeframe;

    /**
     * Permite adicionar metatags a essa instancia. Pode ser usado por exemplo por indicadores ou Candlestick para
     * informar que este item já foi processado pelo algoritmo, evitando processamento desnecessário.
     */
    public final Map<String, Object> meta = new HashMap<>();

    /**
     * No formato "SYMBOL TIME OPEN HIGH LOW CLOSE TICK_VOLUME REAL_VOLUME SPREAD INTERVAL"
     *
     * Ex. "EURUSD 1537429422098 1.88823000 1.88835000 1.00000000 2.00000000 0 0 0 86400"
     *
     * @param data
     */
    public Rate(String data) {
        SDParser p = new SDParser(data, ' ');
        this.symbol = p.pop();
        this.time = p.popLong();
        this.open = p.popDouble();
        this.high = p.popDouble();
        this.low = p.popDouble();
        this.close = p.popDouble();
        this.volumeTick = p.popLong();
        this.volumeReal = p.popLong();
        this.spread = p.popInt();
        this.timeframe = TimeFrame.ofSeconds(p.popInt());
    }

    public Rate(String symbol, long time, double open, double high, double low, double close, long volumeTick, long volumeReal, int spread, TimeFrame timeframe) {
        this.symbol = symbol;
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volumeTick = volumeTick;
        this.volumeReal = volumeReal;
        this.spread = spread;
        this.timeframe = timeframe;
    }

    @Override
    public String toString() {
        return "" + this.time + " " + this.symbol + " OHLC=" + this.open + " " + this.high + " " + this.low + " " + this.close;
    }

}
