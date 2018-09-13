package com.github.nidorx.jtrade.broker.trading;

/**
 * Cada transação é caracterizada por um tipo,
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum DealType {
    /**
     * Compra
     */
    BUY,
    /**
     * Venda
     */
    SELL,
    /**
     * Saldo
     */
    BALANCE,
    /**
     * Crédito
     */
    CREDIT,
    /**
     * Cobrança adicional
     */
    CHARGE,
    /**
     * Correção
     */
    CORRECTION,
    /**
     * Bonus
     */
    BONUS,
    /**
     * Comissão adicional
     */
    COMMISSION,
    /**
     * Taxa de juros
     */
    INTEREST,
    /**
     * Operação de dividendos
     */
    DIVIDEND,
    /**
     * Cálculo do imposto
     */
    TAX,
    /**
     * Tipo de transação não classificada
     */
    OTHER;
}
