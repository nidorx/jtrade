package com.github.nidorx.jtrade.indicator;

import java.time.Instant;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * Representa um buffer de dados ou calculo usado pelo indicador
 *
 * Um buffer pode ser de dados (output) ou de uso interno (para fins de calculo)
 *
 * @see Indicator#createBuffer(boolean)
 * @see Indicator#getOutput()
 */
public class IndicatorBuffer {

    private final SortedMap<Instant, IndicatorOutput> output = new TreeMap<>();

    /**
     * Define um valor para o instante
     *
     * @param instant
     * @param value
     */
    public void value(Instant instant, Double value) {
        Integer color = null;
        if (output.containsKey(instant)) {
            color = output.get(instant).color;
        }

        output.put(instant, new IndicatorOutput(value, color));
    }

    /**
     * Obtém um valor do buffer para o instante informado
     *
     * @param instant
     * @return
     */
    public Double value(Instant instant) {
        IndicatorOutput out = output.get(instant);
        return out == null ? null : out.value;
    }

    /**
     * Define uma cor para o instante
     *
     * @param instant
     * @param color
     */
    public void color(Instant instant, Integer color) {
        Double value = null;
        if (output.containsKey(instant)) {
            value = output.get(instant).value;
        }

        output.put(instant, new IndicatorOutput(value, color));
    }

    /**
     * Obtém uma cor para o instante informado
     *
     * @param instant
     * @return
     */
    public Integer color(Instant instant) {
        IndicatorOutput out = output.get(instant);
        return out == null ? null : out.color;
    }

    /**
     * Permite iterar nos valores de saída
     *
     * @param action
     */
    public void forEachOutput(BiConsumer<Instant, IndicatorOutput> action) {
        output.forEach(action);
    }
}
