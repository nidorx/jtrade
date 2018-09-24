package com.github.nidorx.jtrade.broker.trading;

import java.time.Instant;

/**
 * Transação – fato da compra ou venda de um instrumento financeiro.
 *
 * Uma transação é o reflexo de uma execução de uma negociação baseada em uma ordem que contém uma solicitação de
 * negociação.
 *
 * A compra (Buy) acontece segundo o preço de demanda (Ask) e a venda acontece segundo o preço de oferta (Bid).
 *
 * A transação pode ser feita como resultado da execução de uma ordem de mercado ou ativação de uma operação pendente.
 *
 * Em alguns casos, várias transações podem ser o resultado da execução de uma única ordem.
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class Deal {

    /**
     * O identificador dessa transação. Um número exclusivo atribuído a cada transação.
     */
    public final Long id;

    /**
     * O identificador da ordem que gerou essa transação
     */
    public final Long order;

    /**
     * O instante da execução da transação
     */
    public final Instant time;

    /**
     * O tipo de transação. Cada transação é caracterizada por um tipo,
     */
    public final DealType type;

    /**
     * As transações (deal) diferem entre si não somente no seu conjunto de tipos em {@link DealType}, mas também na
     * forma como elas alteram posições.
     */
    public final DealEntry entry;

    /**
     * Preço da transação
     */
    public final double price;

    /**
     * Volume da transação
     */
    public final double volume;

    /**
     * Comissão da transação
     */
    public final double commission;

    /**
     * Swap acumulativo no fechamento
     */
    public final double swap;

    /**
     * Lucro da transação
     */
    public final double profit;

    public Deal(Long id, Long order, Instant time, DealType type, DealEntry entry, double price, double volume, double commission, double swap, double profit) {
        this.id = id;
        this.order = order;
        this.time = time;
        this.type = type;
        this.entry = entry;
        this.price = price;
        this.volume = volume;
        this.commission = commission;
        this.swap = swap;
        this.profit = profit;
    }
}
