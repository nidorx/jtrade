package com.github.nidorx.jtrade.broker.trading;

import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.broker.Instrument;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * As solicitações para executar operações de negociação são formalizadas como ordens.
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@Data
public class Order {

    @Getter(AccessLevel.NONE)
    private final Broker broker;

    /**
     * As transações executadas nesta ordem
     */
    private final List<Deal> deals = new ArrayList<>();

    /**
     * O identificador único da ordem. Um número exclusivo atribuído a cada ordem
     */
    private final Long id;

    /**
     * O instrumento de negociação da ordem
     */
    private final Instrument instrument;

    /**
     * Tipo de ordem
     */
    private final OrderType type;

    /**
     * O estado de execução desta ordem
     */
    private OrderState state;

    /**
     * A Política de execução da ordem
     */
    private OrderFilling filling;

    /**
     * O identificador da posição, quando a ordem é executada
     */
    private Long position;

    /**
     * O instante em que a ordem foi enviada, executada ou cancelada
     */
    private Instant time;

    /**
     * Preço especificado na ordem
     */
    private double price;

    /**
     * Volume corrente de uma ordem
     */
    private double volume;

    /**
     * Valor de Stop Loss
     */
    private double stopLoss;

    /**
     * Valor de Take Profit
     */
    private double takeProfit;

    public Order(Broker broker, Long id, OrderType type, Instrument instrument, double price, double volume) {
        this.broker = broker;
        this.id = id;
        this.type = type;
        this.instrument = instrument;
        this.price = price;
        this.volume = volume;
    }

    public Long getId() {
        return id;
    }

    public OrderType getType() {
        return type;
    }

    public void addDeal(Deal deal) {
        deals.add(deal);
    }

    public List<Deal> getDeals() {
        return new ArrayList<>(deals);
    }

    public void modify(double price, double volume) throws TradeException {
        broker.modify(this, price, volume);
    }

    public void modify(double price, double volume, double sl, double tp) throws TradeException {
        broker.modify(this, price, volume, sl, tp);
    }

    public void modifyStop(double sl, double tp) throws TradeException {
        broker.modifyStop(this, sl, tp);
    }

    public void remove() throws TradeException {
        broker.remove(this);
    }

    /**
     * Calcula o lucro/prejuizo dessa ordem
     *
     * @return
     */
    public double profit() {
        return deals.stream()
                .mapToDouble(deal -> deal.getProfit())
                .sum();
    }
}
