package com.github.nidorx.jtrade.core.exception;

/**
 * @see https://www.mql5.com/en/docs/constants/errorswarnings/enum_trade_return_codes
 * @see https://www.mql5.com/en/articles/2555
 *
 * @author Alex
 */
public enum TradeExceptionReason {
    /**
     * Request rejected
     */
    REJECT,
    /**
     * Request processing error
     */
    ERROR,
    /**
     * Request canceled by timeout
     */
    TIMEOUT,
    /**
     * Invalid request
     */
    INVALID,
    /**
     * Invalid volume in the request
     */
    INVALID_VOLUME,
    /**
     * Invalid price in the request
     */
    INVALID_PRICE,
    /**
     * Invalid stops in the request
     */
    INVALID_STOPS,
    /**
     * Trade is disabled
     */
    TRADE_DISABLED,
    /**
     * Market is closed
     */
    MARKET_CLOSED,
    /**
     * There is not enough money to complete the request
     */
    NO_MONEY,
    /**
     * Prices changed
     */
    PRICE_CHANGED,
    /**
     * There are no quotes to process the request
     */
    PRICE_OFF,
    /**
     * Order state changed
     */
    ORDER_CHANGED,
    /**
     * Too frequent requests
     */
    TOO_MANY_REQUESTS,
    /**
     * No changes in request
     *
     * Sending a trade request which does not make any changes is considered an error.
     */
    NO_CHANGES,
    /**
     * Request locked for processing
     */
    LOCKED,
    /**
     * Order or position frozen
     */
    FROZEN,
    /**
     * Invalid order filling type
     */
    INVALID_FILL,
    /**
     * No connection with the trade server
     */
    CONNECTION,
    /**
     * The number of pending orders has reached the limit
     */
    LIMIT_ORDERS,
    /**
     * The volume of orders and positions for the symbol has reached the limit
     */
    LIMIT_VOLUME,
    /**
     * Incorrect or prohibited order type
     */
    INVALID_ORDER,
    /**
     * Position with the specified POSITION_IDENTIFIER has already been closed
     */
    POSITION_CLOSED,
    /**
     * A close volume exceeds the current position volume
     */
    INVALID_CLOSE_VOLUME,
    /**
     * The number of open positions simultaneously present on an account can be limited by the server settings.
     *
     * When a limit is reached, the platform does not let placing new orders whose execution may increase the number of
     * open positions.
     */
    LIMIT_POSITIONS,
    /**
     * The pending order activation request is rejected, the order is canceled
     */
    REJECT_CANCEL,
    /**
     * The request is rejected, because the "Only long positions are allowed" rule is set for the symbol
     */
    LONG_ONLY,
    /**
     * The request is rejected, because the "Only short positions are allowed" rule is set for the symbol
     */
    SHORT_ONLY,
    /**
     * The request is rejected, because the "Only position closing is allowed" rule is set for the symbol
     */
    CLOSE_ONLY
}
