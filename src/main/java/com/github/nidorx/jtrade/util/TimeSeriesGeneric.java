package com.github.nidorx.jtrade.util;

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
 * Implementação genérica para gerenciamento de TimeSéries
 *
 * @author Alex Rodin <contato@alexrodin.info>
 * @param <T>
 */
public abstract class TimeSeriesGeneric<T> {

    private final SortedMap<Instant, T> data = Collections.synchronizedSortedMap(new TreeMap<>((a, b) -> {
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
     * Permite extrair o instant do item
     *
     * @param item
     * @return
     */
    protected abstract Instant extract(T item);

    /**
     * Adiciona varios valores na lista ordenada
     *
     * @param items
     */
    public void add(List<T> items) {
        items.forEach(item -> add(item));
    }

    /**
     * Adiciona um valor na lista ordenada
     *
     * @param item
     */
    public void add(T item) {
        final Instant itemInstant = extract(item);

        if (itemInstant == null || data.containsKey(itemInstant)) {
            // Evita processamento desnecessário
            return;
        }

        // Quando for inserido um registro mais antigo do que o ultimo registro salvo, 
        // pode significar que o window frame foi modificado ou registros mais antigos foram adicionados
        // força a atualização dos indicadores
        this.data.put(itemInstant, item);

        // Refaz os indices
        int i = 0;
        for (Instant instant : this.data.keySet()) {
            indexed.put(instant.getEpochSecond(), i);
            indexedReverse.put(i++, instant.getEpochSecond());
        }
    }

    public int size() {
        return data.size();
    }

    public T first() {
        return data.get(data.lastKey());
    }

    public T last() {
        return data.get(data.firstKey());
    }

    public T one(Instant instant) {
        final Instant closestStart = getClosestStart(instant);
        if (!data.containsKey(closestStart)) {
            return null;
        }
        return data.get(closestStart);
    }

    public Stream<Map.Entry<Instant, T>> stream() {
        return data.entrySet().stream();
    }

    public List<T> list() {
        return data.entrySet().stream()
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    public List<T> list(int count) {
        return list(0, count);
    }

    public List<T> list(Instant stop) {
        return list(Instant.ofEpochSecond(indexedReverse.get(0)), stop);
    }

    public List<T> list(int start, int count) {
        if (indexedReverse.size() <= start) {
            return new ArrayList<>();
        }
        return list(Instant.ofEpochSecond(indexedReverse.get(start)), count);
    }

    public List<T> list(Instant start, int count) {
        final Instant closestStart = getClosestStart(start);

        return data.tailMap(closestStart)
                .entrySet().stream()
                .limit(count)
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    public List<T> list(Instant start, Instant stop) {
        final Instant closestStart = getClosestStart(start);
        final Instant closestStop = getClosestStop(stop);

        return takeWhile(closestStart, e -> !e.getKey().isBefore(closestStop))
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

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
     * Permite iterar numa Stream de valores até que a condição seja satisfeita
     *
     * @param <T>
     * @param stream
     * @param predicate
     * @return
     * @see https://stackoverflow.com/a/20765715
     */
    private Stream<Map.Entry<Instant, T>> takeWhile(Instant start, Predicate<Map.Entry<Instant, T>> predicate) {
        final Instant closestStart = getClosestStart(start);
        final Stream<Map.Entry<Instant, T>> stream = data.tailMap(closestStart).entrySet().stream().sequential();
        final Spliterator<Map.Entry<Instant, T>> splitr = stream.spliterator();
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
