package com.github.nidorx.jtrade.backtesting.forex;

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class ForexUtils {

    /**
     * Tamanho do lote padrão
     */
    private static final int STANDARD = 100000;

    /**
     * A função calcula a margem necessária para o tipo de ordem especificado, na conta corrente, no ambiente de mercado
     * atual não tendo em conta os pedidos pendentes atuais e posições abertas.
     *
     * Ele permite a avaliação da margem para a operação comercial prevista.
     *
     * @param volume Em relação ao lote padrão
     * @param price Preço de conversão do ativo/Taxa cambial
     * @param leverage Alavancagem
     * @param lot Tamanho do lote padrão (STANDARD = 100000)
     * @return
     * @see http://escoladeoperadores.com.br/pf/index.php/forex/43-lotes
     */
    public static double orderMargin(double volume, double price, double leverage, double lot) {
        // Margem necessária = Tamanho da operação (trade) / Alavancagem * Taxa cambial da conta
        return (volume * lot) / leverage * price;
    }

    /**
     * A função calcula o lucro para a conta corrente, nas atuais condições de mercado, baseado nos parâmetros
     * passados​​.
     *
     * A função é usado para a pré-avaliação do resultado de uma operação de negócio (comércio).
     *
     * ATENÇÃO! Método só funciona com day trade, o {@link https://pt.wikipedia.org/wiki/Swap Swap} não é levado em
     * conta
     *
     * @param volume Volume da operação, em relação ao lote padrão
     * @param close Preço de abertura da operação
     * @param open Preço de fechamento da operaçao
     * @return
     * @see https://www.fxpro.pt/trading/calculators/profit
     */
    public static double calcOrderProfit(double volume, double open, double close) {
        // Lucro na Divisa da Conta = ((preço_fecho - preço_abertura) * Tamanho posição / (*) Câmbio)         
        // Câmbio = preço_fecho
        return (close - open) * (volume * STANDARD);
    }

    /**
     * A função calcula o lucro percentual
     *
     * @param volume
     * @param open
     * @param close
     * @return
     */
    public static double calcOrderProfitPercent(double volume, double open, double close) {
        // Lucro na Divisa da Conta = ((preço_fecho - preço_abertura) * Tamanho posição / (*) Câmbio)         
        // Câmbio = preço_fecho
        double profit = calcOrderProfit(volume, open, close);
        double entrance = (open) * (volume * STANDARD);
        return profit * 100 / entrance;
    }

    /**
     * Permite calcular o lucro em PIPS de uma operação
     *
     * @param open Preço de abertura
     * @param close Preço de fechamento
     * @param pip Tamanho de pips para a moeda (USDJPY = 0.01, EURUSD = 0.0001)
     * @return
     */
    public static double caclOrderProfitPips(double open, double close, double pip) {
        return (close - open) / pip;
    }

    /**
     * Calcula o valor de 1 PIP para a cotação informada
     *
     * @param price
     * @param pip
     * @return
     */
    public static double caclPipPrice(double price, double pip) {
        return pip / price * STANDARD;
    }

}
