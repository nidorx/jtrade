package com.github.nidorx.jtrade.core;

import com.github.nidorx.jtrade.util.Cancelable;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.broker.trading.Position;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representação de uma estratégia de negociação. Pode ser comparado a um Expert Advisor do MT5 por exemplo
 *
 * @author Alex
 */
public abstract class Strategy {

    /**
     * O contexto de execução
     */
    private Broker broker;

    /**
     * O instrumento de execução atual da estratégia
     */
    public Instrument instrument;

    /**
     * Handle para cancelar recebimento de atualizações do contexto
     */
    private Cancelable brokerListener;

    /**
     * Instante da finalização da execução do onTick
     */
    private Instant onTickEnd;

    /**
     * Indica que está processando o método onTick
     */
    private final AtomicBoolean isOnTick = new AtomicBoolean(false);

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
     * Permite executar a estratégia para cada Tick
     *
     * @param tick
     */
    public abstract void onTick(Tick tick);

    /**
     * Execução da estratégia para cada candle
     *
     * @param rate
     */
    public abstract void onRate(Rate rate);

    /**
     * Permite a implementação do indicador executar quaisquer rotinas de limpeza quando este indicador for desconectado
     * do timeSeries
     */
    protected abstract void onRelease();

    public final Broker getBroker() {
        return broker;
    }

    public final Instrument getInstrument() {
        return instrument;
    }

    /**
     * Remove o handle de execução e contexto desta estratégia.
     *
     * Ao fazer isso, essa estratégia deixa de receber atualizações do {@link Broker} e portanto, não realiza mais
     * operações
     */
    public final void release() {
        if (brokerListener != null) {
            brokerListener.cancel();
            brokerListener = null;
        }

        // Rotinas de limpeza
        this.onRelease();

        this.broker = null;
        this.instrument = null;
        this.onTickEnd = null;
        this.isOnTick.set(false);
    }

    /**
     * Associa esta estratégia em um contexto de execução (Broker)
     *
     * Após isso, sempre que o {@link Broker} receber novos valores, essa estratégia será informada, e realizará o fluxo
     * implmentado
     *
     * @param broker
     * @param symbol
     * @throws Exception
     */
    public final void registerOn(final Broker broker, final String symbol) throws Exception {
        if (!broker.equals(this.broker)) {
            release();
            this.broker = broker;
            brokerListener = broker.register(this, symbol);
        }
        this.instrument = broker.getInstrument(symbol);
    }

    /**
     * Controle de execução do onTick da estratégia.
     *
     * O método onTick não é chamado se o tick a ser processado veio enquanto a estratégia estava processando outro
     * tick, evitando assim processar informações defazadas, aumentando a velocidade de execução do script e garantindo
     * apenas o processamento de dados recentes
     *
     * @param tick
     */
    public final void processTick(final Tick tick) {
        if (this.isOnTick.get()) {
            // Está processando onTick
            return;
        }

        if (!tick.symbol.equalsIgnoreCase(getInstrument().symbol)) {
            // Só permite processar ticks do mesmo símbolo
            return;
        }

        // Se o tick veio antes do fim do processamento anterior, ignora o processamento
        if (this.onTickEnd != null && tick.time.isBefore(this.onTickEnd)) {
            return;
        }

        if (this.isOnTick.compareAndSet(false, true)) {
            // Faz o processamento do tick, single thread
            Instant start = Instant.now();
            this.onTick(tick);

            this.isOnTick.set(false);

            if (broker.getServerTime().equals(tick.time)) {
                // Não recebeu outro tick, computa o tempo de processamento real
                this.onTickEnd = tick.time.plusMillis(Instant.now().toEpochMilli() - start.toEpochMilli());
            } else {
                // @TODO: Contexto pode ser Null
                this.onTickEnd = broker.getServerTime();
            }
        }
    }

    public final List<Position> getPositions() {
        return this.getBroker().getPositions(this.getInstrument());
    }

    public final List<Order> getOrders() {
        return this.getBroker().getOrders(this.getInstrument());
    }

    public final void buy(double volume) throws TradeException {
        this.getBroker().buy(this.getInstrument(), this.getInstrument().ask(), volume, 0);
    }

    public final void buy(double price, double volume, long deviation) throws TradeException {
        this.getBroker().buy(this.getInstrument(), price, volume, deviation, 0, 0);
    }

    public final void buy(double price, double volume, long deviation, double sl, double tp) throws TradeException {
        this.getBroker().buy(this.getInstrument(), price, volume, deviation, sl, tp);
    }

    public final void sell(double volume) throws TradeException {
        this.getBroker().sell(this.getInstrument(), volume);
    }

    public final void sell(double price, double volume, long deviation) throws TradeException {
        this.getBroker().sell(this.getInstrument(), price, volume, deviation);
    }

    public final void sell(double price, double volume, long deviation, double sl, double tp) throws TradeException {
        this.getBroker().sell(this.getInstrument(), price, volume, deviation, sl, tp);
    }

    public final void buyLimit(double price, double volume) throws TradeException {
        this.getBroker().buyLimit(this.getInstrument(), price, volume);
    }

    public final void buyLimit(double price, double volume, double sl, double tp) throws TradeException {
        this.getBroker().buyLimit(this.getInstrument(), price, volume, sl, tp);
    }

    public final void sellLimit(double price, double volume) throws TradeException {
        this.getBroker().sellLimit(this.getInstrument(), price, volume);
    }

    public final void sellLimit(double price, double volume, double sl, double tp) throws TradeException {
        this.getBroker().sellLimit(this.getInstrument(), price, volume, sl, tp);
    }

    public final void buyStop(double price, double volume) throws TradeException {
        this.getBroker().buyStop(this.getInstrument(), price, volume);
    }

    public final void buyStop(double price, double volume, double sl, double tp) throws TradeException {
        this.getBroker().buyStop(this.getInstrument(), price, volume, sl, tp);
    }

    public final void sellStop(double price, double volume, double sl, double tp) throws TradeException {
        this.getBroker().sellStop(this.getInstrument(), price, volume, sl, tp);
    }

    public final void sellStop(double price, double volume) throws TradeException {
        this.getBroker().sellStop(this.getInstrument(), price, volume);
    }

    public final void modify(Position position, double sl, double tp) throws TradeException {
        this.getBroker().modify(position, sl, tp);
    }

    public final void modify(Order order, double price, double volume) throws TradeException {
        this.getBroker().modify(order, price, volume);
    }

    public final void modify(Order order, double price, double volume, double sl, double tp) throws TradeException {
        this.getBroker().modify(order, price, volume, sl, tp);
    }

    public final void remove(Order order) throws TradeException {
        this.getBroker().remove(order);
    }

    public final void close(Position position, double price, long deviation) throws TradeException {
        this.getBroker().close(position, price, deviation);
    }

    public final void closePartial(Position position, double price, double volume, long deviation) throws TradeException {
        this.getBroker().closePartial(position, price, volume, deviation);
    }

    public final void modifyStop(Order order, double sl, double tp) throws TradeException {
        this.getBroker().modifyStop(order, sl, tp);
    }
}
