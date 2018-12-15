package com.github.nidorx.jtrade.core;

import com.github.nidorx.jtrade.util.function.Cancelable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Série temporal genérica
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
 * @author Alex Rodin <contato@alexrodin.info>
 * @param <T>
 */
public interface TimeSeries<T> {

    /**
     * Permite ser informado quando esta Timeseries receber novos valores.
     *
     * Se já houver registros, o callback é acionado imediatamente
     *
     * @param callback
     * @return
     */
    Cancelable onUpdate(Consumer<Boolean> callback);

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
    T first();

    /**
     * Obtém a informação de preços e volumes mais recente
     *
     * @return
     */
    T last();

    /**
     * Obtém a informação de preços e volumes pra um instante específico, ou null caso não exista valores para o
     * instante informado
     *
     * @param instant
     * @return
     */
    T one(Instant instant);

    /**
     * Permite acesso ao stream dos registros deste TimeSeries
     *
     * @return
     */
    Stream<Map.Entry<Instant, T>> stream();

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
    List<T> list(int count);

    List<T> list(Instant stop);

    List<T> list(int start, int count);

    List<T> list(Instant start, int count);

    List<T> list(Instant start, Instant stop);

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
