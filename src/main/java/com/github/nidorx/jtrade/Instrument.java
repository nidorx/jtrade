package com.github.nidorx.jtrade;

import java.util.Currency;
import lombok.Getter;

/**
 * Representa um instrumento a ser negociado.
 *
 * O instrumento é criado e gerenciado pelo Broker
 *
 * @author Alex
 */
@Getter
public abstract class Instrument {

    /**
     * Símbolo do instrumento
     *
     * ex. EURUSD, USDJPY
     */
    public final String symbol;

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
    public final int contractSize;

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

    /**
     * Moeda em que os requisitos de margem são calculados.
     */
    public final Currency base;

    /**
     * Moeda na qual o lucro do comércio de símbolos é calculado.
     */
    public final Currency quote;

    public Instrument(String symbol, Currency base, Currency quote) {
        this(symbol, base, quote, 4, 100000, 0.0);
    }

    public Instrument(String symbol, Currency base, Currency quote, int digits, int contractSize, double tickValue) {
        this.symbol = symbol;
        this.digits = digits;
        this.contractSize = contractSize;
        this.tickSize = 1 / Math.pow(10, digits);
        this.tickValue = tickValue;
        this.base = base;
        this.quote = quote;
    }

    public double bid() {
        return 0;
    }

    public double ask() {
        return 0;
    }

    public double spread(Instrument instrument) {
        return ask() - bid();
    }

    public double stopLevel() {
        return 0;
    }

    public double freezeLevel() {
        return 0;
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

    /**
     * Obtém um TimeSeries para o instrumento atual.
     *
     * @param timeframe
     * @return
     */
    public TimeSeries getTimeseries(TimeFrame timeframe) {
        return null;
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
