package com.github.nidorx.jtrade.broker.trading;

import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.Instrument;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import java.time.Instant;
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
     * O identificador dessa posição. Um número exclusivo atribuído a cada posição.
     *
     * O Identificador de posição é um número único que é atribuído para toda nova posição aberta e não se altera
     * durante todo o tempo de vida da posição. Movimentações de uma posição não alteram seu identificador.
     */
    private final Long id;

    /**
     * As ordens executadas nesta operação.
     *
     * Pode existir por exemplo uma ordem de venda, e apos alguns segundos executado outra ordem de compra
     */
    private final List<Order> orders;

    /**
     * A direção de da posição em aberto (comprada ou vendida)
     */
    private final PositionType type;

    /**
     * O instante em que a ordem foi enviada, executada ou cancelada
     */
    private Instant time;

    /**
     * O preço de abertura da posição
     */
    private final double price;

    private double stopLoss;

    private double takeProfit;

    public Position(Broker broker, Long id, List<Order> orders, PositionType type, double price) {
        this.broker = broker;
        this.id = id;
        this.orders = new ArrayList<>(orders);
        this.type = type;
        this.price = price;
    }

    public Position(Broker broker, Long id, List<Order> orders, PositionType type, double price, double stopLoss, double takeProfit) {
        this.broker = broker;
        this.id = id;
        this.orders = new ArrayList<>(orders);
        this.type = type;
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

    public void close(double price, long deviation) throws TradeException {
        broker.close(this, price, deviation);
    }

    public void closePartial(double price, double volume, long deviation) throws TradeException {
        broker.closePartial(this, price, volume, deviation);
    }

    /**
     * Calcula o volume sendo negociado atualmente.
     *
     * Somente dos valores já executados das ordens desta posição
     *
     * @return
     */
    public double volume() {
        double volume = orders.stream()
                .filter((order) -> order.getState().filled())
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
     * Obtém o instrumento de negociação desta posição
     *
     * @return
     */
    public Instrument instrument() {
        return orders.stream().findFirst().get().getInstrument();
    }

    /**
     * Calcula o lucro/prejuizo da operação atual
     *
     * @return
     */
    public double profit() {
        return orders.stream()
                .mapToDouble(order -> order.profit())
                .sum();
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
}
