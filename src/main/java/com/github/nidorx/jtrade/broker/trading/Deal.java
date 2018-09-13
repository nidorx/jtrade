package com.github.nidorx.jtrade.broker.trading;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
@Data
@Builder
@AllArgsConstructor
public class Deal {

    /**
     * O identificador dessa transação. Um número exclusivo atribuído a cada transação.
     */
    private final Long id;

    /**
     * O identificador da ordem que gerou essa transação
     */
    private final Long order;

    /**
     * O instante da execução da transação
     */
    private final Instant time;

    /**
     * O tipo de transação. Cada transação é caracterizada por um tipo,
     */
    private final DealType type;

    /**
     * As transações (deal) diferem entre si não somente no seu conjunto de tipos em {@link DealType}, mas também na
     * forma como elas alteram posições.
     */
    private final DealEntry entry;

    /**
     * Preço da transação
     */
    private final double price;

    /**
     * Volume da transação
     */
    private final double volume;

    /**
     * Comissão da transação
     */
    private final double commission;

    /**
     * Swap acumulativo no fechamento
     */
    private final double swap;

    /**
     * Lucro da transação
     */
    private final double profit;
}
