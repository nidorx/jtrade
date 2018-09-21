package com.github.nidorx.jtrade.core;

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
public final class InstrumentImpl extends Instrument {

    private final Map<TimeFrame, TimeSeries> timeSeries = new ConcurrentHashMap<>();

    private final TimeSeriesGeneric<Tick> ticks = new TimeSeriesGeneric<Tick>() {
        @Override
        public Instant extract(Tick item) {
            return item.time;
        }
    };

    private double bid = 0D;

    private double ask = 0D;

    public InstrumentImpl(String symbol, Currency base, Currency quote) {
        super(symbol, base, quote);
        initTimeseries();
    }

    public InstrumentImpl(String symbol, Currency base, Currency quote, int digits, int contractSize, double tickValue) {
        super(symbol, base, quote, digits, contractSize, tickValue);
        initTimeseries();
    }

    @Override
    public double bid() {
        return bid;
    }

    @Override
    public double ask() {
        return ask;
    }

    @Override
    public TimeSeries timeSeries(TimeFrame timeFrame) {
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

    public void processTick(Tick tick) {
        if (tick.symbol.equals(this.symbol)) {
            ticks.add(tick);
            bid = tick.bid;
            ask = tick.ask;
        }
    }

    public void processRate(Rate rate) {
        if (!timeSeries.containsKey(rate.timeframe)) {
            return;
        }

        ((TimeSeriesImpl) timeSeries.get(rate.timeframe)).add(rate);

        // Após o processamento, salva o timeséries em disco, evita re-consultas ao broker
//            final String timeSeriesName = getName() + "_" + instrument.getSymbol() + "_" + timeFrame.name();;
//            persistTimeseries(timeSeriesName, tsGlobal);
    }

    private void initTimeseries() {
        for (TimeFrame timeframe : TimeFrame.values()) {
            timeSeries.put(timeframe, new TimeSeriesImpl());
        }
    }

}
