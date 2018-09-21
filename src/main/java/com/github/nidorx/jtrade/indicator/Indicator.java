package com.github.nidorx.jtrade.indicator;

import com.github.nidorx.jtrade.util.Cancelable;
import com.github.nidorx.jtrade.core.TimeSeries;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Representação de um indicador de uma Timeserie
 *
 * https://www.mql5.com/en/docs/series/indicatorcreate
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public abstract class Indicator {

    /**
     * Permite cancelar o recebimento do {@link TimeSeries#onUpdate(info.alexrodin.lib.Callback) evento de callback}
     */
    private Cancelable cancelListener;

    /**
     * Registra a ultima alteração recebida do TimeSeries
     */
    private int tsLastUpdate = 0;

    /**
     * Registra a ultima execução do método calcular deste indicador
     */
    private int lastCalculated = -1;

    /**
     * Os buffers de saída de dados
     */
    private final List<Buffer> data = new ArrayList<>();

    /**
     * O TimeSeries a que este indicador está associado
     */
    protected TimeSeries timeSeries;

    /**
     * O ultimos instantes calculados por este indicador.
     *
     * Permite que a implementação do indicador verifique os itens calculados anteriormente
     */
    protected List<Instant> calculated = new LinkedList<>();

    /**
     * Executa o processamento do indicador
     *
     * Por definição, o método será executado quando os dados forem requisitados {@link  Indicator#getOutput()}
     *
     * @param instant O instante sendo calculado no momento
     */
    abstract protected void calculate(Instant instant);

    /**
     * Permite a implementação do indicador executar quaisquer rotinas de limpeza quando este indicador for desconectado
     * do timeSeries
     */
    protected void onRelease() {

    }

    /**
     * Remove um handle deste indicador e libera o bloco de cálculo.
     *
     * Ao fazer isso, este indicador deixa de receber atualizações do {@link TimeSeries} e portanto, não realiza mais
     * calculos
     */
    public void release() {
        if (timeSeries != null) {
            cancelListener.cancel();
        }

        // Rotinas de limpeza
        this.onRelease();

        timeSeries = null;
        cancelListener = null;
        tsLastUpdate = 0;
        lastCalculated = -1;
        calculated.clear();
    }

    /**
     * Associa este indicador a uma {@link TimeSeries}.
     *
     * Após isso, sempre que o {@link TimeSeries} receber novos valores este indicador será informado, e realizará a
     * computação quando for solicitado o resultado
     *
     * @param ts
     */
    public void appendTo(final TimeSeries ts) {
        release();
        this.timeSeries = ts;
//        cancelListener = ts.onUpdate((addedOldData) -> {
//            tsLastUpdate++;
//
//            // Quando o timeséries recebe valores antigos, força o re-calculo do indicador
//            if (addedOldData) {
//                lastCalculated = -1;
//                calculated.clear();
//            }
//        });
    }

    /**
     * Obtém os dados de processamento do indicador
     *
     * @return
     */
    public SortedMap<Instant, List<Output>> getOutput() {
        // Verifica se é necessário realizar calculos
        if (tsLastUpdate > lastCalculated) {
            // Obtém os itens que precisam ser processados ainda
            final Instant prev = calculated.isEmpty() ? null : calculated.get(calculated.size() - 1);
            timeSeries.time(prev).stream()
                    // Ordena do mais antigo para o mais novo (no TimeSeries, o indice 0 é o mais recente)
                    .sorted((a, b) -> a.compareTo(b))
                    .forEach(instant -> {
                        if (instant.equals(prev)) {
                            // Já fez este processamento
                            return;
                        }
                        this.calculate(instant);
                        this.calculated.add(instant);
                    });
            lastCalculated = tsLastUpdate;
        }

        final SortedMap<Instant, List<Output>> result = new TreeMap<>();

        data.forEach(buffer -> {
            buffer.forEachOutput((instant, output) -> {
                final List<Output> outputs;
                if (result.containsKey(instant)) {
                    outputs = result.get(instant);
                } else {
                    outputs = new ArrayList<>();
                    result.put(instant, outputs);
                }
                outputs.add(output);
            });
        });
        return result;
    }

    /**
     * Adiciona um buffer ao Indicador.
     *
     * Um buffer pode ser usado para abrigar dados de saída ou apenas valores temporários usados nos calculos internos
     *
     * Para um buffer de dados de saída seus dados serão entregues ao acionar o método
     * {@link Indicator#getOutput() getOutput}
     *
     * @param isOutputBuffer Informa que esta é um buffer de saída
     * @return
     */
    protected final Buffer createBuffer(boolean isOutputBuffer) {
        final Buffer buffer = new Buffer();
        if (isOutputBuffer) {
            data.add(buffer);
        }
        return buffer;
    }
}
