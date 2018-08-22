package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.broker.enums.DealType;
import com.github.nidorx.jtrade.broker.enums.OrderType;
import com.github.nidorx.jtrade.broker.enums.PositionType;
import com.github.nidorx.jtrade.broker.enums.OrderState;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Representação de uma operação de negociação forex.
 *
 * Esta classe é um Cash Flow de uma operação
 *
 * https://www.metatrader5.com/en/terminal/help/general_concept#hedging
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@Data
public class Position {

    @Getter(AccessLevel.NONE)
    private final Broker broker;

    /**
     * As ordens executadas nesta operação.
     *
     * Pode existir por exemplo uma ordem de venda, e apos alguns segundos executado outra ordem de vendad
     *
     * Formato: Instant -> Order
     */
    private final List<Order> orders = new ArrayList<>();

    private final Long id;

    private final Instrument instrument;

    private final PositionType type;

    /**
     * O preço de abertura da posição
     */
    private final double price;

    private double stopLoss;

    private double takeProfit;

    public Position(Broker broker, Long id, PositionType type, Instrument instrument, double price) {
        this.broker = broker;
        this.id = id;
        this.type = type;
        this.instrument = instrument;
        this.price = price;
    }

    public Position(Broker broker, Long id, PositionType type, Instrument instrument, double price, double stopLoss, double takeProfit) {
        this.broker = broker;
        this.id = id;
        this.type = type;
        this.instrument = instrument;
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public void modify(double sl, double tp) throws TradeException {
        broker.modify(this, sl, tp);
    }

    public void close(Position position, double price, long deviation) throws TradeException {
        broker.close(this, price, deviation);
    }

    public void closePartial(Position position, double price, double volume, long deviation) throws TradeException {
        broker.closePartial(this, price, volume, deviation);
    }

    /**
     * Calcula o volume sendo negociado atualmente. Somente dos valores já executados das ordens desta posição
     *
     * @return
     */
    public double volume() {
        double volume = orders.stream()
                .filter((order) -> {
                    return order.getState() == OrderState.FILLED;
                })
                .mapToDouble((order) -> {
                    return order.getDeals().stream()
                            .mapToDouble((deal) -> {
                                if (deal.getType().equals(DealType.BUY)) {
                                    return deal.getVolume();
                                } else if (deal.getType().equals(DealType.SELL)) {
                                    return deal.getVolume() * -1;
                                }
                                return 0.0;
                            }).sum();
                })
                .sum();
        return (type == PositionType.SELL) ? (volume * -1) : volume;
    }
    /**
     * Verifica se todas as ordens desta posição estão fechadas
     *
     * @return
     */
//    public boolean closed() {
//        return orders.stream()
//                .filter((o) -> !OrderState.CLOSED.equals(o.getState()))
//                .count() == 0;
//    }
    /**
     * Calcula o lucro/prejuizo da operação atual a partir do preço informado
     *
     * @param price
     * @return
     */
//    public double profit(double price) {
//        return orders.stream()
//                .mapToDouble(order -> order.profit(price))
//                .sum();
//    }
}
