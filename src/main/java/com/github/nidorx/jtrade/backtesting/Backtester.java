package com.github.nidorx.jtrade.backtesting;

import com.github.nidorx.jtrade.util.Cancelable;
import com.github.nidorx.jtrade.backtesting.forex.BacktesterForexBroker;
import com.github.nidorx.jtrade.Strategy;
import com.github.nidorx.jtrade.TimeFrame;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.broker.Account;
import com.github.nidorx.jtrade.Instrument;
import java.time.Instant;

/**
 *
 * @author Alex
 */
public class Backtester {

    public static void run(
            Strategy strategy,
            Account initialAccount,
            Instrument instrument,
            TimeFrame timeFrame
    ) throws Exception {
        run(strategy, initialAccount, Instant.MIN, Instant.now());
    }

    public static void run(
            Strategy strategy,
            Account initialAccount,
            Instant start,
            Instant end
    ) throws Exception {
        BacktesterBroker broker = new BacktesterForexBroker();
        broker.initialize(initialAccount, start, end);
        Cancelable cancelable = broker.register(strategy);

        while (broker.nextTick()) {
            // Faz alguma coisa? Verifica status da conta e etc? Log?              
        }

        cancelable.cancel();
    }

}
