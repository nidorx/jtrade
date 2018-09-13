package com.github.nidorx.jtrade.broker.trading;

/**
 * A Política de execução da ordem
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public enum OrderFilling {
    /**
     * Tudo/Nada (Fill or Kill)
     *
     * Essa política de execução indica que a ordem pode ser executada exclusivamente no volume especificado.
     *
     * Se, no mercado, nesse momento, não estiver presente um volume suficiente de instrumento financeiro, a ordem não
     * será executada. O volume necessário pode ser composto de algumas propostas disponíveis nesse momento no mercado.
     *
     * Esta política de preenchimento significa que uma ordem pode ser preenchida somente na quantidade especificada. Se
     * a quantidade desejada do ativo não está disponível no mercado, a ordem não será executada. O volume requerido
     * pode ser preenchido usando várias ofertas disponíveis no mercado no momento.
     */
    FOK,
    /**
     * Tudo/Parcial (Immediate or Cancel)
     *
     * Nesse caso, o trader concorda com a execução da transação pelo volume máximo disponível no mercado, nos limites
     * do indicado na ordem.
     *
     * No caso de impossibilidade de execução total, a ordem será executada no volume disponível, e o volume não
     * disponível será cancelado.
     *
     * A possibilidade de execução de ordens é determinada no servidor de negociação.
     *
     * Este modo significa que um negociador concorda em executar uma operação com o volume máximo disponível no mercado
     * conforme indicado na ordem. No caso do volume integral de uma ordem não puder ser preenchido, o volume disponível
     * dele será preenchido, e o volume restante será cancelado.
     */
    IOC, 
    RETURN;
}
