package com.github.nidorx.jtrade;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Permite trabalhar com séries temporais de ohlc.
 *
 * A ordenação do {@link TimeSeries} difere de um array de dados comum pela ORDENAÇÃO REVERSA.
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
public interface TimeSeries {

    /**
     * Retorna a quantidade de registros existentes no timeseries
     *
     * @return
     */
    int size();

    /**
     * Obtém a informação de preços e volumes mais antigo
     *
     * @return
     */
    Rate first();

    /**
     * Obtém a informação de preços e volumes mais recente
     *
     * @return
     */
    Rate last();

    /**
     * Obtém a informação de preços e volumes pra um instante específico, ou null caso não exista valores para o
     * instante informado
     *
     * @param instant
     * @return
     */
    Rate one(Instant instant);

    /**
     * Permite acesso ao stream dos registros deste TimeSeries
     *
     * @return
     */
    Stream<Map.Entry<Instant, Rate>> stream();

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

    /**
     * Gets history data of Rates.
     *
     * Call by the first position and the number of required elements.
     *
     * The elements ordering of the copied data is from present to the past, i.e., starting position of 0 means the
     * current bar.
     *
     * https://www.mql5.com/en/docs/series/copyrates
     *
     *
     *
     * @param count
     * @return
     */
    List<Rate> rates(int count);

    List<Rate> rates(Instant stop);

    List<Rate> rates(int start, int count);

    List<Rate> rates(Instant start, int count);

    List<Rate> rates(Instant start, Instant stop);

    /**
     * https://www.mql5.com/en/docs/series/copytime
     *
     * @param count
     * @return
     */
    Set<Instant> time(int count);

    Set<Instant> time(Instant stop);

    Set<Instant> time(int start, int count);

    Set<Instant> time(Instant start, int count);

    Set<Instant> time(Instant start, Instant stop);

}
