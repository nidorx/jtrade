package com.github.nidorx.jtrade.core.trading;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Representação de uma operação de negociação forex.
 *
 * Esta classe é um Cash Flow de uma operação
 *
 * https://www.metatrader5.com/en/terminal/help/general_concept#hedging
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class Position {

    /**
     * O identificador dessa posição. Um número exclusivo atribuído a cada posição.
     *
     * O Identificador de posição é um número único que é atribuído para toda nova posição aberta e não se altera
     * durante todo o tempo de vida da posição. Movimentações de uma posição não alteram seu identificador.
     */
    public final Long id;

    /**
     * O instante em que a ordem foi enviada, executada ou cancelada
     */
    public final Instant time;

    /**
     * A direção de da posição em aberto (comprada ou vendida)
     */
    public final PositionType type;

    /**
     * O preço de abertura da posição
     */
    public final double price;

    public final double stopLoss;

    public final double takeProfit;

    /**
     * As ordens executadas nesta operação.
     *
     * Pode existir por exemplo uma ordem de venda, e apos alguns segundos executado outra ordem de compra
     */
    private final List<Order> orders;

    public Position(Long id, Instant time, PositionType type, double price, double stopLoss, double takeProfit, List<Order> orders) {
        this.id = id;
        this.time = time;
        this.type = type;
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    /**
     * Calcula o volume sendo negociado atualmente.
     *
     * Somente dos valores já executados das ordens desta posição
     *
     * @return
     */
    public double volume() {
//        double volume = orders.stream()
//                .filter((order) -> order.getState().filled())
//                .mapToDouble((order) -> {
//                    return order.getDeals().stream()
//                            .mapToDouble((deal) -> {
//                                if (deal.getType().equals(DealType.BUY)) {
//                                    return deal.getVolume();
//                                } else if (deal.getType().equals(DealType.SELL)) {
//                                    return deal.getVolume() * -1;
//                                }
//                                return 0.0;
//                            }).sum();
//                })
//                .sum();
//        return (type == PositionType.SELL) ? (volume * -1) : volume;
        return 0d;
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
