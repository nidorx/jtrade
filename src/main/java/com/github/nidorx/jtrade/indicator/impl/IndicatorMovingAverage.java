package com.github.nidorx.jtrade.indicator.impl;

import com.github.nidorx.jtrade.indicator.AppliedPrice;
import com.github.nidorx.jtrade.indicator.Buffer;
import com.github.nidorx.jtrade.indicator.Indicator;
import java.time.Instant;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Moving Average.
 *
 * O Indicador Técnico Moving Average mostra o valor médio de preços em um certo período de tempo. Quando se calcula a
 * moving average, ela faz a média dos preços em um certo período de tempo. Quando os preços mudam, a moving average
 * aumenta ou diminui.
 *
 * Existem quatro tipos diferentes de moving averages, ver {@link MovingAverageIndicator.METHOD}
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class IndicatorMovingAverage extends Indicator {

    private final Buffer output;

    private final int period;

    private final METHOD method;

    private final AppliedPrice appliedPrice;

    public IndicatorMovingAverage(int period, METHOD method) {
        this(period, method, AppliedPrice.CLOSE);
    }

    public IndicatorMovingAverage(int period, METHOD method, AppliedPrice appliedPrice) {
        this.period = period;
        this.method = method;
        this.appliedPrice = appliedPrice;

        this.output = this.createBuffer(true);
    }

    @Override
    protected void calculate(Instant instant) {
        switch (method) {
            case SMA:
                sma(instant);
                break;
            case EMA:
                ema(instant);
                break;
            case SMMA:
                smma(instant);
                break;
            case LWMA:
                lwma(instant);
                break;
        }
    }

    /**
     * Simple Moving Average
     * <p>
     *
     * Simple, ou em outras palavras, arithmetical moving average, é calculada através da soma dos preços de fechamento
     * ao longo de um certo número de períodos individuais (por exemplo, 12 horas). Este valor é então dividido pelo
     * número de tais períodos.
     *
     * <p>
     * <code>SMA = SUM( FECHAMENTO(i), N) / N</code>
     * <p>
     * Onde:
     * <p>
     * <code>SUM</code> – soma;
     * <p>
     * <code>FECHAMENTO(i)</code> – preço de fechamento (Ou {@link AppliedPrice AppliedPrice} informado) do período
     * atual;
     * <p>
     * <code>N</code> – número de períodos de cálculo.
     *
     * @param instant
     */
    private void sma(Instant instant) {
        // Soma dos preços no periodo
        final DoubleAdder sum = new DoubleAdder();

        // initial accumulation
        timeSeries.rates(instant, period).stream().forEach(ohlc -> {
            sum.add(appliedPrice.apply(ohlc));
        });

        output.value(instant, sum.doubleValue() / Math.min(period, timeSeries.size()));
    }

    /**
     * Exponential Moving Average
     * <p>
     *
     * Exponential Moving Average é calculada pela soma de uma determinada parte do preço de fechamento atual, no valor
     * anterior da moving average. Com a exponential Moving Average, os últimos preços de fechamento são de maior valor.
     * P - a porcentagem da exponential Moving Average será semelhante a:
     * <p>
     *
     * <code>EMA = (FECHAMENTO (i) * P) + (EMA (i - 1) * (1 - P))</code>
     * <p>
     * Onde:
     * <p>
     * <code>EMA (i - 1)</code> – valor da moving average do período anterior;;
     * <p>
     * <code>FECHAMENTO(i)</code> – preço de fechamento (Ou {@link AppliedPrice AppliedPrice} informado) do período
     * atual;
     * <p>
     * <code>P</code> – porcentagem de uso do valor do preço.
     *
     * @param instant
     * @see
     * http://www.dummies.com/personal-finance/investing/stocks-trading/how-to-calculate-exponential-moving-average-in-trading/
     */
    private void ema(Instant instant) {
        double exponent = 2.0 / (period + 1);

        final double price = appliedPrice.apply(timeSeries.last());

        if (calculated.isEmpty()) {
            // Primeiro registro (mais antigo), o EMA nao possui valores
            output.value(instant, price);

        } else {

            Instant prevInstant = calculated.get(calculated.size() - 1);
            double emaPrev = output.value(prevInstant);
            double emaActual = price * exponent + emaPrev * (1 - exponent);
            output.value(instant, emaActual);
        }
    }

    /**
     * Smoothed Moving Average
     *
     * @param instant
     */
    private void smma(Instant instant) {

    }

    /**
     * Linear Weighted Moving Average
     *
     * @param instant
     */
    private void lwma(Instant instant) {

    }

    /**
     * Tipos de Moving Average
     */
    public enum METHOD {
        /**
         * Simple Moving Average
         */
        SMA,
        /**
         * Exponential Moving Average
         */
        EMA,
        /**
         * Smoothed Moving Average
         */
        SMMA,
        /**
         * Linear Weighted Moving Average
         */
        LWMA
    }

}
