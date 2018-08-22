package com.github.nidorx.jtrade;

import com.github.nidorx.jtrade.util.Cancelable;
import com.github.nidorx.jtrade.broker.Account;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.broker.Instrument;

/**
 *
 * @author Alex
 */
public abstract class Strategy {

    /**
     * Handle para cancelar recebimento de atualizações do contexto
     */
    private Cancelable contextListener;

    /**
     * O contexto de execução
     */
    protected Broker context;

    /**
     * Os dados sendo usados na estratégia, recebidas do contexto
     */
    protected TimeSeries timeSeries;

    /**
     * O timeframe de execução atual da estratégia
     */
    public final TimeFrame timeFrame;

    /**
     * O instrumento de execução atual da estratégia
     */
    public final Instrument instrument;

    /**
     * Permite a implementação do indicador executar quaisquer rotinas de limpeza quando este indicador for desconectado
     * do timeSeries
     */
    protected abstract void onRelease();

    /**
     * Obtém o nome da estratégia, usado para LOG de execução
     *
     * @return
     */
    public abstract String getName();

    /**
     * Permite a inicialização da estrategia, como criação de indicadores e etc.
     *
     * @param account Estado inicial da conta de negociação
     */
    public abstract void initialize(Account account);

    /**
     * Execução da estratégia para cada candle
     *
     * @param ohlc
     */
    public abstract void onData(OHLC ohlc);

    public Strategy(TimeFrame timeFrame, Instrument instrument) {
        this.timeFrame = timeFrame;
        this.instrument = instrument;
    }

    /**
     * Remove um handle deste indicador e libera o bloco de cálculo.
     *
     * Ao fazer isso, este indicador deixa de receber atualizações do {@link TimeSeries} e portanto, não realiza mais
     * calculos
     */
    /**
     * Remove o handle de execução e contexto desta estratégia.
     *
     * Ao fazer isso, essa estratégia deixa de receber atualizações do {@link Broker} e portanto, não realiza mais
     * operações
     */
    public void release() {
        if (contextListener != null) {
            contextListener.cancel();
            contextListener = null;

            // Rotinas de limpeza
            this.onRelease();
            this.context = null;
            this.timeSeries = null;
        }
    }

    /**
     * Associa esta estratégia em um contexto de execução (Broker)
     *
     * Após isso, sempre que o {@link Broker} receber novos valores, essa estratégia será informada, e realizará o fluxo
     * implmentado
     *
     * @param context
     * @throws Exception
     */
    public void appendTo(Broker context) throws Exception {
        release();
        this.context = context;
        contextListener = context.register(this);
    }

    public void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

}
