package com.github.nidorx.jtrade.indicator;

/**
 * Representação dos dados de saída de um Indicador
 *
 * @author Alex
 */
public class IndicatorOutput {

    public final Double value;

    public final Integer color;

    public IndicatorOutput(Double value, Integer color) {
        this.value = value;
        this.color = color;
    }
}
