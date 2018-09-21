package com.github.nidorx.jtrade.core;

/**
 * Representação dos Timeframes padrão
 *
 * @author Alex
 */
public enum TimeFrame {
    /**
     * 1 minute
     */
    M1(60),
    /**
     * 5 minutes
     */
    M5(M1.seconds * 5),
    /**
     * 15 minutes
     */
    M15(M1.seconds * 15),
    /**
     * 30 minutes
     */
    M30(M1.seconds * 30),
    /**
     * 1 hour
     */
    H1(M1.seconds * 60),
    /**
     * 4 hours
     */
    H4(H1.seconds * 4),
    /**
     * 1 day
     */
    D1(H1.seconds * 24),
    /**
     * 1 week, 7 days
     */
    W1(D1.seconds * 7),
    /**
     * 1 month, 30 days
     */
    MN1(D1.seconds * 30);

    public final int seconds;

    public final int minutes;

    private TimeFrame(int seconds) {
        this.seconds = seconds;
        this.minutes = seconds / 60;
    }

    public static TimeFrame ofSeconds(int seconds) {
        for (TimeFrame tf : values()) {
            if (tf.seconds == seconds) {
                return tf;
            }
        }
        return null;
    }
}
