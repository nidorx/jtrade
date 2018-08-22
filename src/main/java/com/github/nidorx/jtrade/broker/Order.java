package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.broker.enums.OrderType;
import com.github.nidorx.jtrade.broker.enums.OrderState;
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

    private final List<Deal> deals = new ArrayList<>();

    /**
     * O identificador único da ordem
     */
    private final Long id;

    private final OrderType type;

    private final Instrument instrument;

    private OrderState state;

    /**
     * O identificador da posição, quando a ordem é executada
     */
    private Long position;

    /**
     * O instante em que a ordem foi enviada
     */
    private Instant timeSetup;

    /**
     * O instante em que a ordem foi executada
     */
    private Instant timeDone;

    private double price;

    private double volume;

    private double stopLoss;

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

    public void modifyStop(Order order, double sl, double tp) throws TradeException {
        broker.modifyStop(this, sl, tp);
    }

    public void modify(double price, double volume, double sl, double tp) throws TradeException {
        broker.modify(this, price, volume, sl, tp);
    }

    public void remove() throws TradeException {
        broker.remove(this);
    }

}
