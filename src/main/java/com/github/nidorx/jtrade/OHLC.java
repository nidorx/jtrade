package com.github.nidorx.jtrade;

import java.util.HashMap;
import java.util.Map;

/**
 * Armazena as informações sobre Preço e volume de um ativo em um determinado periodo
 *
 * @TODO: Volume
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

    /**
     * Permite adicionar metatags a essa instancia. Pode ser usado por exemplo por indicadores ou Candlestick para
     * informar que este item já foi processado pelo algoritmo, evitando processamento desnecessário.
     */
    public final Map<String, Object> meta = new HashMap<>();

    public OHLC(long time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

}
