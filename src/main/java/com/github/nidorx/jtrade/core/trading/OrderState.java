package com.github.nidorx.jtrade.core.trading;

/**
 * Os estados possíveis de uma ordem. Cada ordem tem um status que descreve o seu estado.
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum OrderState {
    /**
     * Ordem verificada, mas ainda não aceita pela corretora (broker)
     */
    STARTED,
    /**
     * Ordem aceita
     */
    PLACED,
    /**
     * Ordem cancelada pelo cliente
     */
    CANCELED,
    /**
     * Ordem executada parcialmente
     */
    PARTIAL,
    /**
     * Ordem executada completamente
     */
    FILLED,
    /**
     * Ordem rejeitada
     */
    REJECTED,
    /**
     * Ordem expirada
     */
    EXPIRED,
    /**
     * Ordem está sendo registrada (aplicação para o sistema de negociação)
     */
    REQUEST_ADD,
    /**
     * Ordem está sendo modificada (alterando seus parâmetros)
     */
    REQUEST_MODIFY,
    /**
     * Ordem está sendo excluída (excluindo a partir do sistema de negociação)
     */
    REQUEST_CANCEL;

    public boolean filled() {
        return this == FILLED || this == PARTIAL;
    }

}
