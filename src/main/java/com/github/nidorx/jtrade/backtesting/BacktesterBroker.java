package com.github.nidorx.jtrade.backtesting;

import com.github.nidorx.jtrade.broker.Account;
import com.github.nidorx.jtrade.broker.Broker;
import java.time.Instant;

/**
 * Especificação para Broker usado em Backtesting
 *
 * @author Alex
 */
public abstract class BacktesterBroker extends Broker {

    /**
     * Permite a inicialização do Broker de teste
     *
     * @param account
     * @param start
     * @param end
     * @throws Exception
     */
    public abstract void initialize(Account account, Instant start, Instant end) throws Exception;

    /**
     * Quando acionado, o Broker de teste deve invocar o método
     * {@link Broker#onData(deep.nidorx.core.ta.broker.Instrument, deep.nidorx.core.ta.TimeFrame, deep.nidorx.core.ta.OHLC) onData}
     *
     * @return false se não houver mais tick para processar
     * @throws Exception
     */
    public abstract boolean nextTick() throws Exception;
}
