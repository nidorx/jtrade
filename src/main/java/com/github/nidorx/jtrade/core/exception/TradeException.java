package com.github.nidorx.jtrade.core.exception;

/**
 * Request processing error
 *
 * @author Alex
 */
public class TradeException extends Exception {

    private final TradeExceptionReason reason;

    public TradeException(TradeExceptionReason reason) {
        super();
        this.reason = reason;
    }

    public TradeException(TradeExceptionReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public TradeException(TradeExceptionReason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public TradeException(TradeExceptionReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public TradeExceptionReason getReason() {
        return reason;
    }

}
