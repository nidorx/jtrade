package com.github.nidorx.jtrade.broker.impl.metatrader.model;

/**
 * Lista dos comandos conhecidos pleo EA
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum Command {

    SYMBOL("Symbol", 2),
    BUY("Buy", 3),
    SELL("Sell", 4),
    BUY_LIMIT("BuyLimit", 5),
    SELL_LIMIT("SellLimit", 6),
    BUY_STOP("BuyStop", 7),
    SELL_STOP("SellStop", 8),
    MODIFY_POSITION("ModifyPosition", 9),
    MODIFY_ORDER("ModifyOrder", 10),
    REMOVE("Remove", 11),
    CLOSE("Close", 12),
    CLOSE_PARTIAL("ClosePartial", 13);

    public final String name;

    public final int code;

    private Command(String name, int code) {
        this.name = name;
        this.code = code;
    }

}
