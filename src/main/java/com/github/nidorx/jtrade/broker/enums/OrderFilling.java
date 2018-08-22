package com.github.nidorx.jtrade.broker.enums;

/**
 *
 * @author Alex
 */
public enum OrderFilling {
    /**
     * Fill Or Kill
     *
     * A fill-or-kill order will either fill in its entirety or be completely aborted
     *
     * This filling policy means that an order can be filled only in the specified amount. If the necessary amount of a
     * financial instrument is currently unavailable in the market, the order will not be executed. The required volume
     * can be filled using several offers available on the market at the moment.
     */
    FOK,
    /**
     * Immediate Or Cancel
     *
     * An immediate-or-cancel order can be partially or completely filled, but any portion of the order that cannot be
     * filled immediately will be canceled rather than left on the order book.
     *
     * This mode means that a trader agrees to execute a deal with the volume maximally available in the market within
     * that indicated in the order. In case the the entire volume of an order cannot be filled, the available volume of
     * it will be filled, and the remaining volume will be canceled.
     */
    IOC,
    RETURN;
}
