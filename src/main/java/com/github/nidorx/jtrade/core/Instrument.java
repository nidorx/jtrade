package com.github.nidorx.jtrade.core;

import java.time.Instant;
import java.util.List;

/**
 * Representa um instrumento a ser negociado.
 *
 * O instrumento é criado e gerenciado pelo Broker
 *
 * @author Alex
 */
public abstract class Instrument {

    /**
     * Símbolo do instrumento
     *
     * ex. EURUSD, USDJPY
     */
    public final String symbol;

    /**
     * Moeda em que os requisitos de margem são calculados.
     */
    public final String base;

    /**
     * Moeda na qual o lucro do comércio de símbolos é calculado.
     */
    public final String quote;

    /**
     * Número de casas decimais no preço do símbolo.
     *
     * ex. EURUSD = 4, USDJPY = 2
     */
    public final int digits;

    /**
     * Número de unidades da mercadoria, moeda ou ativo financeiro em um lote.
     *
     * ex. FOREX STANDARD = 100.000
     */
    public final double contractSize;

    /**
     * PIP - Passo de mudança de preço mínimo.
     *
     * ex. EURUSD = 0.0001, USDJPY = 0.01
     */
    public final double tickSize;

    /**
     * Custo de um único ponto de mudança de preço.
     *
     * ex. FOREX STANDARD EURUSD 1.0 USD
     */
    public final double tickValue;

    public Instrument(String symbol, String base, String quote) {
        this(symbol, base, quote, 4, 100000D, 0.0);
    }

    public Instrument(String symbol, String base, String quote, int digits, double contractSize, double tickValue) {
        this.symbol = symbol;
        this.digits = digits;
        this.contractSize = contractSize;
        this.tickValue = tickValue;
        this.base = base;
        this.quote = quote;
        this.tickSize = 1 / Math.pow(10, digits);
    }

    public abstract double bid();

    public abstract double ask();

    /**
     * Minimal permissible StopLoss/TakeProfit value in points.
     *
     * channel of prices (in points) from the current price, inside which one can't place Stop Loss, Take Profit and
     * pending orders. When placing an order inside the channel, the server will return message "Invalid Stops" and will
     * not accept the order.
     *
     * @return
     */
    public abstract int stopLevel();

    /**
     * Order freeze level in points.
     *
     * If the execution price lies within the range defined by the freeze level, the order cannot be modified, canceled
     * or closed.
     *
     * @return
     */
    public abstract int freezeLevel();

    /**
     * Obtém um TimeSeries para o instrumento atual no timeframe informado
     *
     * @param timeframe
     * @return
     */
    public abstract TimeSeriesRate timeSeries(TimeFrame timeframe);

    /**
     * Obtém os ticks disponíveis deste instrumento
     *
     * @return
     */
    public abstract List<Tick> ticks();

    /**
     * Obtém os ticks até o instante determinado
     *
     * @param stop
     * @return
     */
    public abstract List<Tick> ticks(Instant stop);

    /**
     * Obtém os ticks para o intervalo determinado
     *
     * @param start
     * @param stop
     * @return
     */
    public abstract List<Tick> ticks(Instant start, Instant stop);

    public double spread() {
        return ask() - bid();
    }

    /**
     * Obtém o valor de um PIP
     *
     * Se a corretora trabalha com pipetes (decimo de pip), faz a correção e entrega o valor em PIP's, nunca em Pipette
     *
     * ex. 1 PIP EURUSD = 0.0001, 1 PIP USDJPY = 0.01
     *
     * @return
     */
    public double pip() {
        double pip = tickSize;
        if (digits == 3 || digits == 5) {
            pip *= 10;
        }
        return pip;
    }

    /**
     * Obtém o valor monetario da quantidade de PIPS informado, util por exemplo para calcular o valor monetário de 30
     * pips de stop loss
     *
     * @param pips
     * @return
     */
    public double pipsToPrice(double pips) {
        return pips * pip();
    }

    /**
     * Converte os pips informados para points
     *
     * @param pips
     * @return
     */
    public double pipsToPoint(double pips) {
        return pips * pip() / tickSize;
    }

    /**
     * Convete os points informado para o valor na moeda
     *
     * @param points
     * @return
     */
    public double pointsToPrice(double points) {
        return pipsToPrice(pointsToPips(points));
    }

    /**
     * Converte os pontos informados para pips
     *
     * @param points
     * @return
     */
    public double pointsToPips(double points) {
        return points / pip() * tickSize;
    }

    public double ceil(double value) {
        return Math.ceil(value / tickSize) * tickSize;
    }

    public double floor(double value) {
        return Math.floor(value / tickSize) * tickSize;
    }

//    public double valueAtPrice(double pips) {
//        final double price = bid();
//        //  (Contract * (Price + One point)) - (Contract * Price)
//        return pips * (getContractSize() * (price + getTickSize())) - (getContractSize() * price);
//    }
//
//    public double valueAtPrice(double pips) {
//        final double price = bid();
//        //  (Contract * (Price + One point)) - (Contract * Price)
//        return pips * (getContractSize() * (price + getTickSize())) - (getContractSize() * price);
//    }
}
