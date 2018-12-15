package com.github.nidorx.jtrade.backtesting;

/**
 * Após o término do backtest, são calculados diferentes parâmetros das estatísticas dos resultados de negociação.
 *
 * Todos os valores estatísticos do tipo double são expressos na moeda de depósito.
 *
 * @author nidorx
 */
public class TestingStatistics {

    /**
     * O valor do depósito inicial
     */
    private double initialDeposit;

    /**
     * Dinheiro retirado de uma conta
     */
    private double withdrawal;

    /**
     * Lucro líquido após o teste, a soma do Lucro Total ("grossProfit") e Perda Total ("grossLoss"). Perda total é
     * sempre menor ou igual a zero
     */
    private double profit;

    /**
     * Lucro total, a soma de todas as negociações lucrativas (positivas). O valor é sempre maior ou igual a zero
     */
    private double grossProfit;

    /**
     * Perda total, a soma de todos as negociações negativos. O valor é sempre menor ou igual a zero
     */
    private double grossLoss;

    /**
     * Lucro máximo, o maior valor de todas as negociações lucrativas. O valor é maior ou igual a zero
     */
    private double maxProfitTrade;

    /**
     * Perda máxima, O menor valor de todas as negociações negativas. O valor é menor ou igual a zero
     */
    private double maxLossTrade;

    /**
     * Lucro máximo em uma série de negociações lucrativas. O valor é maior ou igual a zero
     */
    private double consequentialProfitMax;

    /**
     * O número de negociações que formaram lucro máximo em uma série de negociações lucrativas (consequentialProfitMax)
     */
    private int consequentialProfitMaxTrades;

    /**
     * O lucro total da mais longa série de negociações lucrativas
     */
    private double consequentialProfitGross;

    /**
     * O número de negociações da mais longa série de negociações lucrativas (consequentialProfitGross)
     */
    private int consequentialProfitGrossTrades;

    /**
     * Comprimento médio das série de negociações lucrativas
     */
    private int consequentialProfitAverage;

    /**
     * Perda máxima em uma série de negociações negativas. O valor é menor ou igual a zero
     */
    private double consequentialLossMax;

    /**
     * O número de negociações que formaram a perda máxima em uma série de negociações negativas (consequentialLossMax)
     */
    private int consequentialLossMaxTrades;

    /**
     * A perda total da mais longa série de negociações negativas
     */
    private double consequentialLossGross;

    /**
     * O número de negociações na série mais longa de negociações negativas (consequentialLossGross)
     */
    private int consequentialLossGrossTrades;

    /**
     * Comprimento médio das séries de negociações negativas
     */
    private int consequentialLossAverage;

    /**
     * Valor do saldo mínimo
     */
    private double balanceMin;

    /**
     * Redução máxima do saldo em termos monetários. No processo de negociação, um saldo pode ter inúmeras reduções;
     * aqui o maior valor é levado em conta
     */
    private double balanceDrawdown;

    /**
     * Redução do saldo como uma porcentagem que foi registrada no momento da redução máxima do saldo em termos
     * monetários (balanceDrawdown).
     */
    private double balanceDrawdownPercent;

    /**
     * Redução relativa em termos monetários que foi registrado no momento do levantamento da redução relativa máxima de
     * saldo como uma porcentagem (balanceDrawdownRelativePercent).
     */
    private double balanceDrawdownRelative;

    /**
     * Redução relativa máxima de saldo como uma porcentagem. No processo de negociação, um saldo pode ter inúmeros
     * rebaixamentos, para cada um dos quais o valor de rebaixamento relativo em porcentagens é calculado. O maior valor
     * é retornado.
     *
     * @see https://www.forexfactory.com/showthread.php?p=5325195
     */
    private double balanceDrawdownRelativePercent;

    /**
     * Valor mínimo do patrimônio
     */
    private double equityMin;

    /**
     * Redução máxima de capital em termos monetários. No processo de negociação, numerosos rebaixamentos podem aparecer
     * no patrimônio líquido; aqui o maior valor é levado
     */
    private double equityDrawdown;

    /**
     * Redução máxima em porcentagem que foi registrada no momento da redução máxima de capital em termos monetários
     * (equityDrawdown).
     */
    private double equityDrawdownPercent;

    /**
     * Rebaixamento relativo de capital em termos monetários que foi registrada no momento da redução máxima do
     * patrimônio em porcentagem (equityDrawdownRelativePercent).
     */
    private double equityDrawdownRelative;

    /**
     * Rebaixamento relativo máxima de capital como uma porcentagem. No processo de negociação, um patrimônio pode ter
     * numerosos rebaixamentos, para cada um dos quais o valor de rebaixamento relativo em porcentagens é calculado. O
     * maior valor é retornado
     */
    private double equityDrawdownRelativePercent;

    /**
     * Retorno esperado
     */
    private double expectedPayoff;

    /**
     * Fator de lucro, igual à proporção de grossProfit / grossLoss. Se grossLoss = 0, o fator de lucro é igual a
     * Double.MAX_VALUE
     */
    private double profitFactor;

    /**
     * Fator de recuperação, igual à proporção de profit/balanceDrawdown
     */
    private double recoveryFactor;

    /**
     * Relação de Sharpe
     *
     * @see https://www.investopedia.com/terms/s/sharperatio.asp
     */
    private double sharpeRatio;

    /**
     * Valor mínimo do nível da margem
     */
    private double marginLevelMin;

    /**
     * O número de Transações (fato da compra ou venda de um instrumento financeiro).
     */
    private int deals;

    /**
     * O número de negociações
     */
    private int trades;

    /**
     * Número de negociações lucrativas
     */
    private int tradesProfit;

    /**
     * Número de negociações negativas
     */
    private int tradesLoss;

    /**
     * Número de negociações vendidas (Short)
     */
    private int tradesShort;

    /**
     * Número de negociações vendidas (Short) lucrativas
     */
    private int tradesShortProfit;

    /**
     * Número de negociações compradas (Long)
     */
    private int tradesLong;

    /**
     * Número de negociações compradas (Long) lucrativas
     */
    private int tradesLongProfit;

    public double getInitialDeposit() {
        return initialDeposit;
    }

    public double getWithdrawal() {
        return withdrawal;
    }

    public double getProfit() {
        return profit;
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public double getGrossLoss() {
        return grossLoss;
    }

    public double getMaxProfitTrade() {
        return maxProfitTrade;
    }

    public double getMaxLossTrade() {
        return maxLossTrade;
    }

    public double getConsequentialProfitMax() {
        return consequentialProfitMax;
    }

    public int getConsequentialProfitMaxTrades() {
        return consequentialProfitMaxTrades;
    }

    public double getConsequentialProfitGross() {
        return consequentialProfitGross;
    }

    public int getConsequentialProfitGrossTrades() {
        return consequentialProfitGrossTrades;
    }

    public int getConsequentialProfitAverage() {
        return consequentialProfitAverage;
    }

    public double getConsequentialLossMax() {
        return consequentialLossMax;
    }

    public int getConsequentialLossMaxTrades() {
        return consequentialLossMaxTrades;
    }

    public double getConsequentialLossGross() {
        return consequentialLossGross;
    }

    public int getConsequentialLossGrossTrades() {
        return consequentialLossGrossTrades;
    }

    public int getConsequentialLossAverage() {
        return consequentialLossAverage;
    }

    public double getBalanceMin() {
        return balanceMin;
    }

    public double getBalanceDrawdown() {
        return balanceDrawdown;
    }

    public double getBalanceDrawdownPercent() {
        return balanceDrawdownPercent;
    }

    public double getBalanceDrawdownRelative() {
        return balanceDrawdownRelative;
    }

    public double getBalanceDrawdownRelativePercent() {
        return balanceDrawdownRelativePercent;
    }

    public double getEquityMin() {
        return equityMin;
    }

    public double getEquityDrawdown() {
        return equityDrawdown;
    }

    public double getEquityDrawdownPercent() {
        return equityDrawdownPercent;
    }

    public double getEquityDrawdownRelative() {
        return equityDrawdownRelative;
    }

    public double getEquityDrawdownRelativePercent() {
        return equityDrawdownRelativePercent;
    }

    public double getExpectedPayoff() {
        return expectedPayoff;
    }

    public double getProfitFactor() {
        return profitFactor;
    }

    public double getRecoveryFactor() {
        return recoveryFactor;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public double getMarginLevelMin() {
        return marginLevelMin;
    }

    public int getDeals() {
        return deals;
    }

    public int getTrades() {
        return trades;
    }

    public int getTradesProfit() {
        return tradesProfit;
    }

    public int getTradesLoss() {
        return tradesLoss;
    }

    public int getTradesShort() {
        return tradesShort;
    }

    public int getTradesShortProfit() {
        return tradesShortProfit;
    }

    public int getTradesLong() {
        return tradesLong;
    }

    public int getTradesLongProfit() {
        return tradesLongProfit;
    }

}
