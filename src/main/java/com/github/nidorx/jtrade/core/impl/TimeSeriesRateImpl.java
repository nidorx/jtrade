package com.github.nidorx.jtrade.core.impl;

import com.github.nidorx.jtrade.core.Rate;
import java.time.Instant;
import com.github.nidorx.jtrade.core.TimeSeriesRate;

/**
 * Implementação para permitir que o Broker faça o gerenciamento dos valores do TimeSéries
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public final class TimeSeriesRateImpl extends TimeSeriesAbstract<Rate> implements TimeSeriesRate {

    @Override
    protected Instant extract(Rate item) {
        return item.time;
    }

    @Override
    public double[] open(int count) {
        return list(count).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(Instant stop) {
        return list(stop).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(int start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(Instant start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] open(Instant start, Instant stop) {
        return list(start, stop).stream().mapToDouble(r -> r.open).toArray();
    }

    @Override
    public double[] close(int count) {
        return list(count).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(Instant stop) {
        return list(stop).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(int start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(Instant start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] close(Instant start, Instant stop) {
        return list(start, stop).stream().mapToDouble(r -> r.close).toArray();
    }

    @Override
    public double[] high(int count) {
        return list(count).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(Instant stop) {
        return list(stop).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(int start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(Instant start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] high(Instant start, Instant stop) {
        return list(start, stop).stream().mapToDouble(r -> r.high).toArray();
    }

    @Override
    public double[] low(int count) {
        return list(count).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(Instant stop) {
        return list(stop).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(int start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(Instant start, int count) {
        return list(start, count).stream().mapToDouble(r -> r.low).toArray();
    }

    @Override
    public double[] low(Instant start, Instant stop) {
        return list(start, stop).stream().mapToDouble(r -> r.low).toArray();
    }

}
