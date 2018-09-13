package com.github.nidorx.jtrade.broker.trading;

/**
 * As operações (deal) diferem entre si não somente no seu conjunto de tipos em {@link DealType}, mas também na forma
 * como elas alteram posições. Isto pode ser uma simples abertura de posição, ou acumulação de uma posição aberta
 * anteriormente (entrada de mercado), encerramento de posição através de uma operação oposta no volume correspondente
 * (saída de mercado), ou reversão de posição, se a operação em direção oposta cobrir o volume da posição aberta
 * anteriormente.
 *
 * Todas estas situações são descritas pelos valores deste Enum.
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum DealEntry {
    /**
     * Entrada
     */
    IN,
    /**
     * Saída
     */
    OUT,
    /**
     * Reversão
     */
    INOUT,
    /**
     * Fechamento pela posição oposta
     */
    OUT_BY;
}
