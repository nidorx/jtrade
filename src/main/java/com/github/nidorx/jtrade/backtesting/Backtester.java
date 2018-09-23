package com.github.nidorx.jtrade.backtesting;

import com.github.nidorx.jtrade.core.Strategy;
import com.github.nidorx.jtrade.core.TimeFrame;
import com.github.nidorx.jtrade.core.Account;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.broker.trading.Position;
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
    public Position getPosition(Instrument instrument) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double stopLevel(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double freezeLevel(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order buyLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order sellLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order sellStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void modify(Order order, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Order order) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void modify(Position position, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close(Position position, double price, long deviation) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closePartial(Position position, double price, double volume, long deviation) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Order buyStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
