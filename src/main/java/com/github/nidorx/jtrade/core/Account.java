package com.github.nidorx.jtrade.core;

import com.github.nidorx.jtrade.util.SDParser;
import java.time.Instant;
import java.util.Currency;

/**
 * Informações sobre uma conta de negociação em um instante específico
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class Account {

    public final Instant time;

    public final String currency;

    /**
     * Determina a alavancagem dessa conta
     */
    public final double leverage;

    /**
     * Saldo da conta na moeda de depósito
     */
    public final double balance;

    public final double equity;

    /**
     * Margem da conta usada na moeda de depósito
     */
    public final double margin;

    /**
     * Margem livre de uma conta na moeda de depósito
     */
    public final double marginFree;

    /**
     * Crédito na conta
     */
    public final double profit;

    /**
     * No formato "TIME_SECONDS CURRENCY LEVERAGE BALANCE EQUITY MARGIN MARGIN_FREE PROFIT"
     *
     * Ex. "EURUSD 1537429422098 0.88823000 0.88835000 0.00000000 0"
     *
     * @param data
     */
    public Account(String data) {
        final SDParser p = new SDParser(data, ' ');
        this.time = Instant.ofEpochSecond(p.popLong());
        this.currency = p.pop();
        this.leverage = p.popDouble();
        this.balance = p.popDouble();
        this.equity = p.popDouble();
        this.margin = p.popDouble();
        this.marginFree = p.popDouble();
        this.profit = p.popDouble();
    }

    public Account(Instant time, String currency, double leverage, double balance, double equity, double margin, double marginFree, double profit) {
        this.time = time;
        this.currency = currency;
        this.leverage = leverage;
        this.balance = balance;
        this.equity = equity;
        this.margin = margin;
        this.marginFree = marginFree;
        this.profit = profit;
    }

}
