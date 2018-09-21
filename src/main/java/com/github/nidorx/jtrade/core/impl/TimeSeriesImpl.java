package com.github.nidorx.jtrade.core.impl;

import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.TimeSeries;
import com.github.nidorx.jtrade.util.TimeSeriesGeneric;
import java.time.Instant;
import java.util.List;

/**
 * Implementação para permitir que o Broker faça o gerenciamento dos valores do TimeSéries
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public final class TimeSeriesImpl extends TimeSeriesGeneric<Rate> implements TimeSeries {

    @Override
    protected Instant extract(Rate item) {
        return Instant.ofEpochMilli(item.time);
    }

    @Override
    public double[] open(int count) {
        return rates(count).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(Instant stop) {
        return rates(stop).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(int start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(Instant start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(Instant start, Instant stop) {
        return rates(start, stop).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] close(int count) {
        return rates(count).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(Instant stop) {
        return rates(stop).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(int start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(Instant start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(Instant start, Instant stop) {
        return rates(start, stop).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] high(int count) {
        return rates(count).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(Instant stop) {
        return rates(stop).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(int start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(Instant start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(Instant start, Instant stop) {
        return rates(start, stop).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] low(int count) {
        return rates(count).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(Instant stop) {
        return rates(stop).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(int start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(Instant start, int count) {
        return rates(start, count).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(Instant start, Instant stop) {
        return rates(start, stop).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public List<Rate> rates(int count) {
        return list(count);
    }

    @Override
    public List<Rate> rates(Instant stop) {
        return list(stop);
    }

    @Override
    public List<Rate> rates(int start, int count) {
        return list(start, count);
    }

    @Override
    public List<Rate> rates(Instant start, int count) {
        return list(start, count);
    }

    @Override
    public List<Rate> rates(Instant start, Instant stop) {
        return list(start, stop);
    }

}
