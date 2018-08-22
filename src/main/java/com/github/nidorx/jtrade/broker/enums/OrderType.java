package com.github.nidorx.jtrade.broker.enums;

/**
 *
 * @author Alex
 */
public enum OrderType {
    /**
     * Ordem de Comprar a Mercado
     */
    BUY,
    /**
     * Ordem de Vender a Mercado
     */
    SELL,
    /**
     * Ordem pendente Buy Limit
     */
    BUY_LIMIT,
    /**
     * Ordem pendente Sell Limit
     */
    SELL_LIMIT,
    /**
     * Ordem pendente Buy Stop
     */
    BUY_STOP,
    /**
     * Ordem pendente Sell Stop
     */
    SELL_STOP,
    /**
     * Ao alcançar o preço da ordem, uma ordem pendente Buy Limit é colocada no preço StopLimit
     */
    BUY_STOP_LIMIT,
    /**
     * Ao alcançar o preço da ordem, uma ordem pendente Sell Limit é colocada no preço StopLimit
     */
    SELL_STOP_LIMIT;

    public boolean isBuy() {
        return this.equals(BUY) || this.equals(BUY_LIMIT) || this.equals(BUY_STOP) || this.equals(BUY_STOP_LIMIT);
    }

}
