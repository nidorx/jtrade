package com.github.nidorx.jtrade.core.impl;

import com.github.nidorx.jtrade.core.Instrument;
import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.Tick;
import com.github.nidorx.jtrade.core.TimeFrame;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.github.nidorx.jtrade.core.TimeSeriesRate;

/**
 * Implementação para permitir ao Broker gerenciar o instrumento
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public final class InstrumentImpl extends Instrument {

    private final Map<TimeFrame, TimeSeriesRate> timeSeries = new ConcurrentHashMap<>();

    private final TimeSeriesAbstract<Tick> ticks = new TimeSeriesAbstract<Tick>() {
        @Override
        public Instant extract(Tick item) {
            return item.time;
        }
    };

    private double bid = 0D;

    private double ask = 0D;

    private int stopLevel = 0;

    private int freezeLevel = 0;

    public InstrumentImpl(String symbol, String base, String quote) {
        super(symbol, base, quote);
        initTimeseries();
    }

    public InstrumentImpl(String symbol, String base, String quote, int digits, double contractSize, double tickValue) {
        super(symbol, base, quote, digits, contractSize, tickValue);
        initTimeseries();
    }

    public InstrumentImpl(String symbol, String base, String quote, int digits, double contractSize, double tickValue,
            double bid, double ask) {
        super(symbol, base, quote, digits, contractSize, tickValue);
        this.bid = bid;
        this.ask = ask;
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
    public int stopLevel() {
        return stopLevel;
    }

    @Override
    public int freezeLevel() {
        return freezeLevel;
    }

    public void setStopLevel(int stopLevel) {
        this.stopLevel = stopLevel;
    }

    public void setFreezeLevel(int freezeLevel) {
        this.freezeLevel = freezeLevel;
    }

    @Override
    public TimeSeriesRate timeSeries(TimeFrame timeFrame) {
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

        ((TimeSeriesRateImpl) timeSeries.get(rate.timeframe)).add(rate);

        // Após o processamento, salva o timeséries em disco, evita re-consultas ao broker
//            final String timeSeriesName = getName() + "_" + instrument.getSymbol() + "_" + timeFrame.name();;
//            persistTimeseries(timeSeriesName, tsGlobal);
    }

    private void initTimeseries() {
        for (TimeFrame timeframe : TimeFrame.values()) {
            timeSeries.put(timeframe, new TimeSeriesRateImpl());
        }
    }

}
