package com.github.nidorx.jtrade.ta.indicator;

import com.github.nidorx.jtrade.util.function.Cancelable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import com.github.nidorx.jtrade.core.TimeSeriesRate;

/**
 * Representação de um indicador de uma Timeserie
 *
 * https://www.mql5.com/en/docs/series/indicatorcreate
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public abstract class Indicator {

    /**
     * Permite cancelar o recebimento do {@link TimeSeriesRate#onUpdate(info.alexrodin.lib.Callback) evento de callback}
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
    private IndicatorBuffer[] data;

    /**
     * O TimeSeries a que este indicador está associado
     */
    protected TimeSeriesRate timeSeries;

    /**
     * O ultimos instantes calculados por este indicador.
     *
     * Permite que a implementação do indicador verifique os itens calculados anteriormente
     */
    protected List<Instant> calculated = new ArrayList<>();

    /**
     * Necessário determinar a quantidade de buffers que o Indiacador vai usar para dados.
     *
     * Essa informação pode ser usada por ferramentas para calculo da quantidade de dados usados em processamento por
     * exemplo
     *
     * @return
     */
    abstract public int getQtdBuffers();

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
     * Ao fazer isso, este indicador deixa de receber atualizações do {@link TimeSeriesRate} e portanto, não realiza mais
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
     * Associa este indicador a uma {@link TimeSeriesRate}.
     *
     * Após isso, sempre que o {@link TimeSeriesRate} receber novos valores este indicador será informado, e realizará a
     * computação quando for solicitado o resultado
     *
     * @param ts
     */
    public void appendTo(final TimeSeriesRate ts) {
        release();
        this.timeSeries = ts;
        cancelListener = ts.onUpdate((addedOldData) -> {
            tsLastUpdate++;

            // Quando o timeséries recebe valores antigos, força o re-calculo do indicador
            if (addedOldData) {
                lastCalculated = -1;
                calculated.clear();
            }
        });
    }

    /**
     * Obtém os dados de processamento do indicador, com ordenação invertida, assim como o TimeSeries (Mais recente =
     * indice 0)
     *
     * @return
     */
    public final List<Map<Instant, IndicatorOutput>> getOutput() {
        // Verifica se é necessário realizar calculos
        if (tsLastUpdate > lastCalculated) {

            // Ordena de forma inversa a lista
            Collections.sort(calculated, (a, b) -> {
                // Indice 0 deve ser o valor mais recente
                return b.compareTo(a);
            });

            // Obtém os itens que precisam ser processados ainda
            final Instant prev = calculated.isEmpty() ? null : calculated.get(0);
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

        final List<Map<Instant, IndicatorOutput>> result = new ArrayList<>();

        for (int i = 0; i < getQtdBuffers(); i++) {
            final IndicatorBuffer buffer = getBuffer(i);
            final SortedMap<Instant, IndicatorOutput> bufferOutputs = Collections.synchronizedSortedMap(new TreeMap<>((a, b) -> {
                // Indice 0 deve ser o valor mais recente
                return b.compareTo(a);
            }));
            
            buffer.forEachOutput((instant, output) -> {
                bufferOutputs.put(instant, output);
            });
            
            result.add(bufferOutputs);
        }

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
     * @param index Se index menor que zero ou maior ou igual a getQtdBuffers, não é um buffer de saída.
     * @return
     */
    protected final IndicatorBuffer getBuffer(int index) {
        if (index < 0 || index >= getQtdBuffers()) {
            return new IndicatorBuffer();
        }

        if (data == null) {
            data = new IndicatorBuffer[getQtdBuffers()];
        }

        if (data[index] == null) {
            data[index] = new IndicatorBuffer();
        }

        return data[index];
    }
}
