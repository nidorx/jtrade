package com.github.nidorx.jtrade.core.trading;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * As solicitações para executar operações de negociação são formalizadas como ordens.
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class Order {

    /**
     * O identificador único da ordem. Um número exclusivo atribuído a cada ordem
     */
    public final Long id;

    /**
     * O identificador da posição, quando a ordem é executada
     */
    public final Long position;

    /**
     * O instante em que a ordem foi enviada, executada ou cancelada
     */
    public final Instant time;

    /**
     * Tipo de ordem
     */
    public final OrderType type;

    /**
     * O estado de execução desta ordem
     */
    public final OrderState state;

    /**
     * A Política de execução da ordem
     */
    public final OrderFilling filling;

    /**
     * Preço especificado na ordem
     */
    public final double price;

    /**
     * Volume corrente de uma ordem
     */
    public final double volume;

    /**
     * Valor de Stop Loss
     */
    public final double stopLoss;

    /**
     * Valor de Take Profit
     */
    public final double takeProfit;

    /**
     * The Limit order price for the StopLimit order
     */
    public final double stopLimit;

    /**
     * As transações executadas nesta ordem
     */
    private final List<Deal> deals = new ArrayList<>();

    public Order(Long id, Long position, Instant time, OrderType type, OrderState state, OrderFilling filling,
            double price, double volume, double stopLoss, double takeProfit, double stopLimit) {
        this.id = id;
        this.position = position;
        this.time = time;
        this.type = type;
        this.state = state;
        this.filling = filling;
        this.price = price;
        this.volume = volume;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.stopLimit = stopLimit;
    }

    public List<Deal> getDeals() {
        return deals;
    }

    /**
     * Calcula o lucro/prejuizo dessa ordem
     *
     * @return
     */
    public double profit() {
        return deals.stream()
                .mapToDouble(deal -> deal.profit)
                .sum();
    }
}
