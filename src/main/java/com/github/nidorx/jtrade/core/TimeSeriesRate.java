package com.github.nidorx.jtrade.core;

import java.time.Instant;

/**
 * Permite trabalhar com séries temporais de ohlc.
 *
 * A ordenação do {@link TimeSeriesRate} difere de um array de dados comum pela ORDENAÇÃO REVERSA.
 *
 * <p>
 * Os elementos das séries temporais são indexados do final de um array para seu início (a partir do mais recente para o
 * mais antigo), isto é, a posição de início 0 significa o registro OHLC mais recente.
 *
 * <p>
 * Ex.: <code>[...][5][4][3][2][1][0]</code>, onde <code>[0]</code> é o registro mais recente.
 *
 * @author Alex
 */
public interface TimeSeriesRate extends TimeSeries<Rate> {

    /**
     * https://www.mql5.com/en/docs/series/copyopen
     *
     * @param count
     * @return
     */
    double[] open(int count);

    double[] open(Instant stop);

    double[] open(int start, int count);

    double[] open(Instant start, int count);

    double[] open(Instant start, Instant stop);

    /**
     * https://www.mql5.com/en/docs/series/copyclose
     *
     * @param count
     * @return
     */
    double[] close(int count);

    double[] close(Instant stop);

    double[] close(int start, int count);

    double[] close(Instant start, int count);

    double[] close(Instant start, Instant stop);

    /**
     * https://www.mql5.com/en/docs/series/copyhigh
     *
     * @param count
     * @return
     */
    double[] high(int count);

    double[] high(Instant stop);

    double[] high(int start, int count);

    double[] high(Instant start, int count);

    double[] high(Instant start, Instant stop);

    /**
     * https://www.mql5.com/en/docs/series/copylow
     *
     * @param count
     * @return
     */
    double[] low(int count);

    double[] low(Instant stop);

    double[] low(int start, int count);

    double[] low(Instant start, int count);

    double[] low(Instant start, Instant stop);

}
