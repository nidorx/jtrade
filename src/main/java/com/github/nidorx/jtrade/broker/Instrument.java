package com.github.nidorx.jtrade.broker;

import java.util.Currency;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Representa um instrumento a ser negociado
 *
 * @author Alex
 */
@Getter
public class Instrument {

    @Getter(AccessLevel.NONE)
    private final Broker broker;

    /**
     * Símbolo do instrumento
     *
     * ex. EURUSD, USDJPY
     */
    private final String symbol;

    /**
     * Número de casas decimais no preço do símbolo.
     *
     * ex. EURUSD = 4, USDJPY = 2
     */
    private final int digits;

    /**
     * Número de unidades da mercadoria, moeda ou ativo financeiro em um lote.
     *
     * ex. FOREX STANDARD = 100.000
     */
    private final int contractSize;

    /**
     * PIP - Passo de mudança de preço mínimo.
     *
     * ex. EURUSD = 0.0001, USDJPY = 0.01
     */
    private final double tickSize;

    /**
     * Custo de um único ponto de mudança de preço.
     *
     * ex. FOREX STANDARD EURUSD 1.0 USD
     */
    private final double tickValue;

    /**
     * Moeda em que os requisitos de margem são calculados.
     */
    private final Currency base;

    /**
     * Moeda na qual o lucro do comércio de símbolos é calculado.
     */
    private final Currency quote;

    public Instrument(Broker broker, String symbol, Currency base, Currency quote) {
        this(broker, symbol, base, quote, 4, 100000, 0.0);
    }

    public Instrument(Broker broker, String symbol, Currency base, Currency quote, int digits, int contractSize, double tickValue) {
        this.broker = broker;
        this.symbol = symbol;
        this.digits = digits;
        this.contractSize = contractSize;
        this.tickSize = 1 / Math.pow(10, digits);
        this.tickValue = tickValue;
        this.base = base;
        this.quote = quote;
    }

    public double bid() {
        return broker.bid(this);
    }

    public double ask() {
        return broker.ask(this);
    }

    public double stopLevel() {
        return broker.stopLevel(this);
    }

    public double freezeLevel() {
        return broker.freezeLevel(this);
    }

    public double ceil(double value) {
        return Math.ceil(value / tickSize) * tickSize;
    }

    public double floor(double value) {
        return Math.floor(value / tickSize) * tickSize;
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
