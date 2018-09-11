package com.github.nidorx.jtrade;

/**
 * Armazena as informações sobre Preço de um ativo
 *
 * @author Alex
 */
public class OHLC {

    /**
     * Hora de início do período.
     *
     * Segundos a partir de 1970-01-01T00:00:00Z
     */
    public final long time;

    /**
     * Preço de abertura
     */
    public final double open;

    /**
     * O preço mais alto do período
     */
    public final double high;

    /**
     * O preço mais baixo do período
     */
    public final double low;

    /**
     * Preço de fechamento
     */
    public final double close;

    public OHLC(long time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

}
