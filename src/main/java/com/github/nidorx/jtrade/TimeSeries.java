package com.github.nidorx.jtrade;

import com.github.nidorx.jtrade.util.Cancelable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
public class TimeSeries {

    private Object lock;

    /**
     * Lista ordenada com todos os valores deste símbolo neste timeframe
     */
    private final SortedMap<Instant, OHLC> data = Collections.synchronizedSortedMap(new TreeMap<>((a, b) -> {
        // Indice 0 deve ser o valor mais recente
        return b.compareTo(a);
    }));

    /**
     * Time -> Indice
     */
    private final Map<Long, Integer> indexed = new HashMap<>();

    /**
     * Indice -> Time
     */
    private final Map<Integer, Long> indexedReverse = new HashMap<>();

    /**
     * Lista de callbacks que serão invocados quando novos valores forem adicionados
     */
    private final List<Consumer<Boolean>> callbacks = new ArrayList<>();

    /**
     * Adiciona um valor na lista ordenada
     *
     * @param ohlc
     */
    public void add(OHLC ohlc) {
        if (isLocked()) {
            return;
        }
        final Instant ohlcInstant = Instant.ofEpochSecond(ohlc.time);

        if (data.containsKey(ohlcInstant)) {
            // Já existe registro, evita processamento desnecessário
            return;
        }

        // Quando for inserido um registro mais antigo do que o ultimo registro salvo, 
        // pode significar que o window frame foi modificado ou registros mais antigos foram adicionados
        // força a atualização dos indicadores
        boolean oldValuesAdded = ohlcInstant.isBefore(data.firstKey());
        this.data.put(ohlcInstant, ohlc);

        // Refaz os indices
        int i = 0;
        for (Instant instant : this.data.keySet()) {
            indexed.put(instant.getEpochSecond(), i);
            indexedReverse.put(i++, instant.getEpochSecond());
        }

        // Informa sobre alteração nos registros
        callbacks.forEach(callback -> {
            callback.accept(oldValuesAdded);
        });
    }

    public void add(long time, double open, double high, double low, double close) {
        add(new OHLC(time, open, high, low, close));
    }

    /**
     * Adiciona varios valores na lista ordenada
     *
     * @param ohlcs
     */
    public void add(List<OHLC> ohlcs) {
        if (isLocked()) {
            return;
        }
        ohlcs.forEach(ohlc -> add(ohlc));
    }

    /**
     * Permite ser informado quando esta Timeseries receber novos valores.
     *
     * Se já houver registros, o callback é acionado imediatamente
     *
     * @param callback
     * @return
     */
    public Cancelable onUpdate(Consumer<Boolean> callback) {
        callbacks.add(callback);

        // Já executa o callback, se houver registros
        if (!this.data.isEmpty()) {
            callback.accept(true);
        }

        return () -> {
            callbacks.remove(callback);
        };
    }

    /**
     * Permite acesso ao stream dos registros deste TimeSeries
     *
     * @return
     */
    public Stream<Map.Entry<Instant, OHLC>> stream() {
        if (isLocked()) {
            // Se estiver bloqueado, protege o acesso aos registros
            final SortedMap<Instant, OHLC> out = Collections.synchronizedSortedMap(new TreeMap<>((a, b) -> {
                // Indice 0 deve ser o valor mais recente
                return b.compareTo(a);
            }));
            out.putAll(data);
            return out.entrySet().stream();
        } else {
            return data.entrySet().stream();
        }
    }

    /**
     * Retorna a quantidade de registros existentes no timeseries
     *
     * @return
     */
    public int size() {
        return data.size();
    }

    /**
     * Obtém a informação de preços e volumes mais recente
     *
     * @return
     */
    public OHLC last() {
        return data.get(data.firstKey());
    }

    /**
     * Obtém a informação de preços e volumes mais antigo
     *
     * @return
     */
    public OHLC first() {
        return data.get(data.lastKey());
    }

    /**
     * Obtém a informação de preços e volumes pra um instante específico, ou null caso não exista valores para o
     * instante informado
     *
     * @param instant
     * @return
     */
    public OHLC one(Instant instant) {
        final Instant closestStart = getClosestStart(instant);
        if (!data.containsKey(closestStart)) {
            return null;
        }
        return data.get(closestStart);
    }

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
    public List<OHLC> ohlc(int count) {
        return ohlc(0, count);
    }

    public List<OHLC> ohlc(Instant stop) {
        return ohlc(Instant.ofEpochSecond(indexedReverse.get(0)), stop);
    }

    public List<OHLC> ohlc(int start, int count) {
        if (indexedReverse.size() <= start) {
            return new ArrayList<>();
        }
        return ohlc(Instant.ofEpochSecond(indexedReverse.get(start)), count);
    }

    public List<OHLC> ohlc(Instant start, int count) {
        final Instant closestStart = getClosestStart(start);

        return data.tailMap(closestStart)
                .entrySet().stream()
                .limit(count)
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    public List<OHLC> ohlc(Instant start, Instant stop) {
        final Instant closestStart = getClosestStart(start);
        final Instant closestStop = getClosestStop(stop);

        return takeWhile(closestStart, e -> !e.getKey().isBefore(closestStop))
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    /**
     * https://www.mql5.com/en/docs/series/copytime
     *
     * @param count
     * @return
     */
    public Set<Instant> time(int count) {
        return time(0, count);
    }

    public Set<Instant> time(Instant stop) {
        return time(data.firstKey(), stop);
    }

    public Set<Instant> time(int start, int count) {
        if (indexedReverse.size() <= start) {
            return new HashSet<>();
        }
        return time(Instant.ofEpochSecond(indexedReverse.get(start)), count);
    }

    public Set<Instant> time(Instant start, int count) {
        return data.tailMap(getClosestStart(start)).keySet().stream()
                .limit(count)
                .collect(Collectors.toSet());
    }

    public Set<Instant> time(Instant start, Instant stop) {
        final Instant closestStart = getClosestStart(start);
        final Instant closestStop = getClosestStop(stop);

        return takeWhile(closestStart, e -> {
            return !e.getKey().isBefore(closestStop);
        })
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    /**
     * https://www.mql5.com/en/docs/series/copyopen
     *
     * @param count
     * @return
     */
    public double[] open(int count) {
        return ohlc(count).stream().mapToDouble(r -> r.open).toArray();
    }

    public double[] open(Instant stop) {
        return ohlc(stop).stream().mapToDouble(r -> r.open).toArray();
    }

    public double[] open(int start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.open).toArray();
    }

    public double[] open(Instant start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.open).toArray();
    }

    public double[] open(Instant start, Instant stop) {
        return ohlc(start, stop).stream().mapToDouble(r -> r.open).toArray();
    }

    /**
     * https://www.mql5.com/en/docs/series/copyhigh
     *
     * @param count
     * @return
     */
    public double[] high(int count) {
        return ohlc(count).stream().mapToDouble(r -> r.high).toArray();
    }

    public double[] high(Instant stop) {
        return ohlc(stop).stream().mapToDouble(r -> r.high).toArray();
    }

    public double[] high(int start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.high).toArray();
    }

    public double[] high(Instant start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.high).toArray();
    }

    public double[] high(Instant start, Instant stop) {
        return ohlc(start, stop).stream().mapToDouble(r -> r.high).toArray();
    }

    /**
     * https://www.mql5.com/en/docs/series/copylow
     *
     * @param count
     * @return
     */
    public double[] low(int count) {
        return ohlc(count).stream().mapToDouble(r -> r.low).toArray();
    }

    public double[] low(Instant stop) {
        return ohlc(stop).stream().mapToDouble(r -> r.low).toArray();
    }

    public double[] low(int start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.low).toArray();
    }

    public double[] low(Instant start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.low).toArray();
    }

    public double[] low(Instant start, Instant stop) {
        return ohlc(start, stop).stream().mapToDouble(r -> r.low).toArray();
    }

    /**
     * https://www.mql5.com/en/docs/series/copyclose
     *
     * @param count
     * @return
     */
    public double[] close(int count) {
        return ohlc(count).stream().mapToDouble(r -> r.close).toArray();
    }

    public double[] close(Instant stop) {
        return ohlc(stop).stream().mapToDouble(r -> r.close).toArray();
    }

    public double[] close(int start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.close).toArray();
    }

    public double[] close(Instant start, int count) {
        return ohlc(start, count).stream().mapToDouble(r -> r.close).toArray();
    }

    public double[] close(Instant start, Instant stop) {
        return ohlc(start, stop).stream().mapToDouble(r -> r.close).toArray();
    }

    /**
     * Permite bloquear a adição de registros neste TimeSeries.
     *
     * Útil nos testes de estratégias para prevenir a modifição dos dados durante a execução
     *
     * @param owner
     */
    public void lock(Object owner) {
        if (owner == null) {
            return;
        }

        if (lock != null && lock != owner) {
            // Já está bloqueado por outro componente
            return;
        }
        lock = owner;
    }

    /**
     * Permite liberar a adição de registros
     *
     * @param owner
     */
    public void unlock(Object owner) {
        if (owner == null) {
            return;
        }

        if (lock != null && lock != owner) {
            // Etá bloqueado por outro componente
            return;
        }
        // Finalmente, remove o bloqueio
        lock = null;
    }

    private boolean isLocked() {
        return lock != null;
    }

    /**
     * Permite iterar numa Stream de valores até que a condição seja satisfeita
     *
     * @param <T>
     * @param stream
     * @param predicate
     * @return
     * @see https://stackoverflow.com/a/20765715
     */
    private Stream<Map.Entry<Instant, OHLC>> takeWhile(Instant start, Predicate<Map.Entry<Instant, OHLC>> predicate) {
        final Instant closestStart = getClosestStart(start);
        final Stream<Map.Entry<Instant, OHLC>> stream = data.tailMap(closestStart).entrySet().stream().sequential();
        final Spliterator<Map.Entry<Instant, OHLC>> splitr = stream.spliterator();
        final Spliterator iterator = new Spliterators.AbstractSpliterator(splitr.estimateSize(), 0) {

            boolean stillGoing = true;

            @Override
            public boolean tryAdvance(Consumer consumer) {
                if (stillGoing) {
                    boolean hadNext = splitr.tryAdvance(elem -> {
                        if (predicate.test(elem)) {
                            consumer.accept(elem);
                        } else {
                            stillGoing = false;
                        }
                    });
                    return hadNext && stillGoing;
                }
                return false;
            }
        };
        return StreamSupport.stream(iterator, false);
    }

    /**
     * Obtém o item seguinte mais próximo do instante inicial desejado
     *
     * @param start
     * @return
     */
    private Instant getClosestStart(Instant start) {
        if (start == null) {
            return data.firstKey();
        }

        for (Instant time : data.keySet()) {
            if (time.equals(start) || time.isBefore(start)) {
                return time;
            }
        }
        // Não existe registro para o instante solicitado
        return start;
    }

    /**
     * Obtém o item anterior mais próximo do instante final desejado
     *
     * @param stop
     * @return
     */
    private Instant getClosestStop(Instant stop) {
        if (stop == null) {
            return data.lastKey();
        }
        Instant prev = null;
        for (Instant time : data.keySet()) {
            if (time.equals(stop)) {
                return time;
            }
            if (time.isBefore(stop)) {
                return prev == null ? data.firstKey() : prev;
            }
            prev = time;
        }
        return data.lastKey();
    }
}
