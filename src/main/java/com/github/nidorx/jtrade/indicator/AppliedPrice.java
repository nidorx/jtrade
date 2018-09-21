package com.github.nidorx.jtrade.indicator;

import com.github.nidorx.jtrade.Rate;
import java.util.function.Function;

/**
 * Calculations of technical indicators require price values and/or values of volumes, on which calculations will be
 * performed. There are 7 predefined identifiers from the APPLIED enumeration, used to specify the desired price base
 * for calculations.
 */
public class AppliedPrice {

    /**
     * Open price
     */
    public static final AppliedPrice OPEN = new AppliedPrice(r -> r.open);

    /**
     * Close price
     */
    public static final AppliedPrice CLOSE = new AppliedPrice(r -> r.close);

    /**
     * The maximum price for the period
     */
    public static final AppliedPrice HIGH = new AppliedPrice(r -> r.high);

    /**
     * The minimum price for the period
     */
    public static final AppliedPrice LOW = new AppliedPrice(r -> r.low);

    /**
     * Median price, (high + low)/2
     */
    public static final AppliedPrice MEDIAN = new AppliedPrice(r -> (r.high + r.low) / 2);

    /**
     * Typical price, (high + low + close)/3
     */
    public static final AppliedPrice TYPICAL = new AppliedPrice(r -> (r.high + r.low + r.close) / 3);

    /**
     * Weighted close price, (high + low + close + close)/4
     */
    public static final AppliedPrice WEIGHTED = new AppliedPrice(r -> (r.high + r.low + r.close + r.close) / 4);

    private final Function<Rate, Double> apply;

    private AppliedPrice(Function<Rate, Double> apply) {
        this.apply = apply;
    }

    public double apply(Rate rate) {
        return apply.apply(rate);
    }

    public double apply(double open, double close, double high, double low) {
        return apply(new Rate(0, open, high, low, close));
    }

    public enum APPLIED {
        PRICE_OPEN(OPEN),
        PRICE_CLOSE(CLOSE),
        PRICE_HIGH(HIGH),
        PRICE_LOW(LOW),
        PRICE_MEDIAN(MEDIAN),
        PRICE_TYPICAL(TYPICAL),
        PRICE_WEIGHTED(WEIGHTED);

        private final AppliedPrice ref;

        private APPLIED(AppliedPrice ref) {
            this.ref = ref;
        }

        public AppliedPrice get() {
            return ref;
        }
    }

}
