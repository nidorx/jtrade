package com.github.nidorx.jtrade.broker.impl;

import com.github.nidorx.jtrade.Instrument;
import com.github.nidorx.jtrade.Rate;
import com.github.nidorx.jtrade.Tick;
import com.github.nidorx.jtrade.TimeFrame;
import com.github.nidorx.jtrade.TimeSeries;
import com.github.nidorx.jtrade.util.TimeSeriesGeneric;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação para permitir ao Broker gerenciar o instrumento
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class InstrumentImpl extends Instrument {

    private final Map<TimeFrame, TimeSeries> timeSeries;

    private final TimeSeriesGeneric<Tick> ticks = new TimeSeriesGeneric<Tick>() {
        @Override
        public Instant extract(Tick item) {
            return item.time;
        }
    };

    public InstrumentImpl(String symbol, Currency base, Currency quote) {
        super(symbol, base, quote);
        this.timeSeries = new ConcurrentHashMap<>(TimeFrame.all().length);
    }

    public InstrumentImpl(String symbol, Currency base, Currency quote, int digits, int contractSize, double tickValue) {
        super(symbol, base, quote, digits, contractSize, tickValue);
        this.timeSeries = new ConcurrentHashMap<>(TimeFrame.all().length);
    }

    @Override
    public double bid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double ask() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimeSeries timeSeries(TimeFrame timeFrame) {
        if (!timeSeries.containsKey(timeFrame)) {
            return null;
        }

        return timeSeries.get(timeFrame);
    }

    @Override
    public List<Tick> ticks() {
        return ticks.list();
    }

    @Override
    public List<Tick> ticks(Instant stop) {
        return ticks.list(stop);
    }

    @Override
    public List<Tick> ticks(Instant start, Instant stop) {
        return ticks.list(start, stop);
    }

    public void onTick(Tick tick) {
        if (tick.symbol.equals(this.symbol)) {
            ticks.add(tick);
        }
    }

    public void onRate(Rate rate) {
        if (!timeSeries.containsKey(rate.timeframe)) {
            return;
        }

        ((TimeSeriesImpl) timeSeries.get(rate.timeframe)).add(rate);

        // Após o processamento, salva o timeséries em disco, evita re-consultas ao broker
//            final String timeSeriesName = getName() + "_" + instrument.getSymbol() + "_" + timeFrame.name();;
//            persistTimeseries(timeSeriesName, tsGlobal);
    }

}
