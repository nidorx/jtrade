package com.github.nidorx.jtrade.backtesting;

import com.github.nidorx.jtrade.core.Strategy;
import com.github.nidorx.jtrade.core.TimeFrame;
import com.github.nidorx.jtrade.core.Account;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.core.exception.TradeException;
import com.github.nidorx.jtrade.core.trading.Order;
import com.github.nidorx.jtrade.core.trading.Position;
import com.github.nidorx.jtrade.core.Instrument;
import java.time.Instant;

/**
 *
 * @author Alex
 */
public class Backtester extends Broker {

    public static void run(Strategy strategy, Account initialAccount, Instrument instrument, TimeFrame timeFrame) throws Exception {
        run(strategy, initialAccount, Instant.MIN, Instant.now());
    }

    public static void run(Strategy strategy, Account initialAccount, Instant start, Instant end) throws Exception {
//        BacktesterBroker broker = new BacktesterForexBroker();
//        broker.initialize(initialAccount, start, end);
//        Cancelable cancelable = broker.register(strategy);
//
//        while (broker.nextTick()) {
//            // Faz alguma coisa? Verifica status da conta e etc? Log?              
//        }
//
//        cancelable.cancel();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Order order) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close(Position position, double price, long deviation) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
