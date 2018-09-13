package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.broker.trading.Position;
import java.util.Currency;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Informações sobre uma conta de negociação em um instante específico
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@Data
public class Account {

    @Getter(AccessLevel.NONE)
    private final Broker broker;

    private Currency currency;

    /**
     * Determina a alavancagem dessa conta
     */
    private final double leverage;

    /**
     * Crédito na conta
     */
    private final double credit;

    /**
     * Saldo da conta na moeda de depósito
     */
    private final double balance;

    /**
     * Margem da conta usada na moeda de depósito
     */
    private double margin;

    /**
     * Margem livre de uma conta na moeda de depósito
     */
    private double marginFree;

    /**
     * Obtém a posição aberta (se disponível) para o simbolo informado
     *
     * @param instrument
     * @return
     * @throws java.lang.Exception
     */
    public Position getPosition(Instrument instrument) throws Exception {
        return broker.getPosition(instrument);
    }

}
