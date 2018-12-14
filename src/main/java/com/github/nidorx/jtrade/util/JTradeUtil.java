package com.github.nidorx.jtrade.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Métodos utilitários
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class JTradeUtil {

    public static int between(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return (new Random()).nextInt((max - min) + 1) + min;
    }

    public static double between(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return (min + Math.random() * (max - min));
    }

    /**
     * Jogar uma moeda para cima
     *
     * @return
     */
    public static boolean coin() {
        return Math.random() > 0.5;
    }

    /**
     * Jogar uma moeda
     *
     * @param change
     * @return
     */
    public static boolean coin(double change) {
        return Math.random() < change;
    }

    /**
     * Re-mapeia um número que está em um range para outro
     *
     * https://processing.org/reference/map_.html
     *
     * @param value the incoming value to be converted
     * @param currentMin lower bound of the value's current range
     * @param currentMax upper bound of the value's current range
     * @param targetMin lower bound of the value's target range
     * @param targetMax upper bound of the value's target range
     * @return
     */
    public static double remap(double value, double currentMin, double currentMax, double targetMin, double targetMax) {
        return (value - currentMin) / (currentMax - currentMin) * (targetMax - targetMin) + targetMin;
    }

    /**
     * Re-mapeia um número que está em um range para o range 0-1
     *
     * @param value the incoming value to be converted
     * @param currentMin lower bound of the value's current range
     * @param currentMax upper bound of the value's current range
     * @return
     */
    public static double remap(double value, double currentMin, double currentMax) {
        return remap(value, currentMin, currentMax, 0, 1);
    }

    /**
     * Re-mapeia todos os números que estão em um range para o range 0-1
     *
     * @param values the incoming values to be converted
     * @param currentMin lower bound of the value's current range
     * @param currentMax upper bound of the value's current range
     * @return
     */
    public static double[] remap(double[] values, double currentMin, double currentMax) {
        double[] out = new double[values.length];
        for (int i = 0, j = values.length; i < j; i++) {
            out[i] = remap(values[i], currentMin, currentMax);
        }
        return out;
    }

    /**
     * Re-mapeia um número que está no range 0-1 para o range informado
     *
     * @param value the incoming value to be converted
     * @param targetMin lower bound of the value's target range
     * @param targetMax upper bound of the value's target range
     * @return
     */
    public static double unremap(double value, double targetMin, double targetMax) {
        return remap(value, 0, 1, targetMin, targetMax);
    }

    /**
     * Re-mapeia todos os números que estão no range 0-1 para o range informado
     *
     * @param values the incoming values to be converted
     * @param targetMin lower bound of the value's target range
     * @param targetMax upper bound of the value's target range
     * @return
     */
    public static double[] unremap(double[] values, double targetMin, double targetMax) {
        double[] out = new double[values.length];
        for (int i = 0, j = values.length; i < j; i++) {
            out[i] = unremap(values[i], targetMin, targetMax);
        }
        return out;
    }

    public static String time(long time) {

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );
    }

    public static double max(double first, double... rest) {
        double max = first;
        for (double val : rest) {
            max = Math.max(max, val);
        }
        return max;
    }

    public static double min(double first, double... rest) {
        double min = first;
        for (double val : rest) {
            min = Math.min(min, val);
        }
        return min;
    }
}
