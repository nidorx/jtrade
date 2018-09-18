package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.broker.trading.Position;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.util.Cancelable;
import com.github.nidorx.jtrade.util.TrheeConsumer;
import com.github.nidorx.jtrade.OHLC;
import com.github.nidorx.jtrade.Strategy;
import com.github.nidorx.jtrade.TimeFrame;
import com.github.nidorx.jtrade.TimeSeries;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Somente Netting System
 *
 * @author Alex
 */
public abstract class Broker {

    private static final Logger LOGGER = Logger.getLogger(Broker.class.getName());

    /**
     * Cada estratégia possui uma instancia de TimeFrame exclusiva
     */
    private final Map<Strategy, TimeSeries> strategiesTimeSeries = new ConcurrentHashMap<>();

    /**
     * Faz cache de timeséries para instrumento e timeframe
     */
    private final Map<Instrument, Map<TimeFrame, TimeSeries>> timeSeriesCached = new ConcurrentHashMap<>();

    /**
     * Lista de estratégias registradas por instrumento e timeframe
     */
    private final Map<Instrument, Map<TimeFrame, List<Strategy>>> strategiesRegistered = new ConcurrentHashMap<>();

    /**
     * Obtém o nome único do Broker, usado para persistir os TimeSeries em disco
     *
     * @return
     */
    public abstract String getName();

    /**
     * Obtém as informações atualizadas da conta
     *
     * @return
     * @throws java.lang.Exception
     */
    public abstract Account getAccount() throws Exception;

    /**
     * Obtém as informações atualizadas da conta para um instrumento
     *
     * @param instrument
     * @return
     * @throws java.lang.Exception
     */
    public abstract Account getAccountSummary(Instrument instrument) throws Exception;

    /**
     * Permite obter os dados de um instrumento para o período informado
     *
     * @param instrument
     * @param timeFrame
     * @param start
     * @param end
     * @return
     * @throws java.lang.Exception
     */
    protected abstract List<OHLC> requestTimeSeries(Instrument instrument, TimeFrame timeFrame, Instant start, Instant end) throws Exception;

    /**
     * Obtém a posição aberta (se disponível) para o simbolo informado
     *
     * @param instrument
     * @return
     * @throws java.lang.Exception
     */
    public abstract Position getPosition(Instrument instrument) throws Exception;

    /**
     * Obtém a lista de instrumentos negociáveis por este broker
     *
     * @return
     * @throws Exception
     */
    public abstract List<Instrument> getInstruments() throws Exception;

    /**
     * Obtém um instrumento a partir do símbolo informado
     *
     * @param symbol
     * @return
     * @throws Exception
     */
    public abstract Instrument getInstrument(String symbol) throws Exception;

    /**
     * Gets the Account Exchange Rate
     *
     * It serves to convert the profit of a deal in account currency.
     *
     * @param base
     * @param quoted
     * @return
     * @throws java.lang.Exception
     */
    public double exchangeRate(Currency base, Currency quoted) throws Exception {
        Currency accCurrency = getAccount().getCurrency();

        if (accCurrency.equals(base)) {
            // Ex. acc=USD, base = USD, quoted = JPY (USDJPY)
            return getInstrument(base.getCurrencyCode() + quoted.getCurrencyCode()).bid();
        } else if (accCurrency.equals(quoted)) {
            // Ex. acc=USD, base = EUR, quoted = USD (EURUSD)
            return 1;
        } else {
            Instrument instrument = getInstrument(accCurrency.getCurrencyCode() + quoted.getCurrencyCode());
            if (instrument != null) {
                return instrument.bid();
            }
            instrument = getInstrument(quoted.getCurrencyCode() + accCurrency.getCurrencyCode());
            if (instrument != null) {
                return 1 / instrument.bid();
            }
        }

        return 1;
    }

    public double exchange(double value, Currency from, Currency to) throws Exception {
        return value * exchangeRate(from, to);
    }

    /**
     * Obtém as Ordem ativas para o símbolo informado
     *
     * @param instrument
     * @return
     * @throws Exception
     */
    public List<Order> getOrders(Instrument instrument) throws Exception {
        final Position position = getPosition(instrument);
        if (position == null) {
            return null;
        }
        return position.getOrders();
    }

    public OHLC tick(Instrument instrument) {
        for (TimeFrame timeFrame : TimeFrame.all()) {
            OHLC tick = tick(instrument, timeFrame);
            if (tick != null) {
                return tick;
            }
        }
        return null;
    }

    public OHLC tick(Instrument instrument, TimeFrame timeFrame) {
        if (!timeSeriesCached.containsKey(instrument)) {
            return null;
        }
        Map<TimeFrame, TimeSeries> get = timeSeriesCached.get(instrument);
        if (!get.containsKey(timeFrame)) {
            return null;
        }
        return get.get(timeFrame).last();
    }

    public abstract double bid(Instrument instrument);

    public abstract double ask(Instrument instrument);

    public double spread(Instrument instrument) {
        return ask(instrument) - bid(instrument);
    }

    /**
     * Minimal permissible StopLoss/TakeProfit value in points.
     *
     * channel of prices (in points) from the current price, inside which one can't place Stop Loss, Take Profit and
     * pending orders. When placing an order inside the channel, the server will return message "Invalid Stops" and will
     * not accept the order.
     *
     * @param instrument
     * @return
     */
    public abstract double stopLevel(Instrument instrument);

    /**
     * Order freeze level in points.
     *
     * If the execution price lies within the range defined by the freeze level, the order cannot be modified, canceled
     * or closed.
     *
     * @param instrument
     * @return
     */
    public abstract double freezeLevel(Instrument instrument);

    /**
     * Market Execution
     *
     * @param instrument
     * @param volume
     * @return
     * @throws TradeException
     */
    public Order buy(Instrument instrument, double volume) throws TradeException {
        return buy(instrument, ask(instrument), volume, 0);
    }

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @return
     * @throws TradeException
     */
    public Order buy(Instrument instrument, double price, double volume, long deviation) throws TradeException {
        return buy(instrument, price, volume, deviation, 0, 0);
    }

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @param sl
     * @param tp
     * @return
     * @throws TradeException
     */
    public abstract Order buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException;

    /**
     * Market Execution
     *
     * @param instrument
     * @param volume
     * @return
     * @throws TradeException
     */
    public Order sell(Instrument instrument, double volume) throws TradeException {
        return sell(instrument, bid(instrument), volume, 0);
    }

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @return
     * @throws TradeException
     */
    public Order sell(Instrument instrument, double price, double volume, long deviation) throws TradeException {
        return sell(instrument, price, volume, deviation, 0, 0);
    }

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @param sl
     * @param tp
     * @return
     * @throws TradeException
     */
    public abstract Order sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException;

    /**
     * @see Broker#buyLimit(deep.nidorx.core.ta.instrument.Instrument, double, double, long, double, double)
     * @param instrument
     * @param price
     * @param volume
     * @return
     * @throws TradeException
     */
    public Order buyLimit(Instrument instrument, double price, double volume) throws TradeException {
        return buyLimit(instrument, price, volume, 0, 0);
    }

    /**
     * Buy Limit - Pending Order
     * <p>
     * a trade request to buy at the Ask price that is equal to or less than that specified in the order. The current
     * price level is higher than the value specified in the order. Usually this order is placed in anticipation of that
     * the security price will fall to a certain level and then will increase;
     *
     * @param instrument
     * @param price
     * @param volume
     * @param sl
     * @param tp
     * @return
     * @throws TradeException
     */
    public abstract Order buyLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public Order sellLimit(Instrument instrument, double price, double volume) throws TradeException {
        return sellLimit(instrument, price, volume, 0, 0);
    }

    public abstract Order sellLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public Order buyStop(Instrument instrument, double price, double volume) throws TradeException {
        return buyStop(instrument, price, volume, 0, 0);
    }

    public abstract Order buyStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public Order sellStop(Instrument instrument, double price, double volume) throws TradeException {
        return sellStop(instrument, price, volume, 0, 0);
    }

    public abstract Order sellStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public void modify(Order order, double price, double volume) throws TradeException {
        modify(order, price, volume, 0, 0);
    }

    public void modifyStop(Order order, double sl, double tp) throws TradeException {
        modify(order, 0, 0, sl, tp);
    }

    public abstract void modify(Order order, double price, double volume, double sl, double tp) throws TradeException;

    public abstract void remove(Order order) throws TradeException;

    public abstract void modify(Position position, double sl, double tp) throws TradeException;

    public abstract void close(Position position, double price, long deviation) throws TradeException;

    public abstract void closePartial(Position position, double price, double volume, long deviation) throws TradeException;

    /**
     * Registra uma estratégia para ser executada neste contexto.
     *
     * A estratégia passará a receber atualizações do contexto e executar transações no Broker deste contexto
     *
     * @param strategy
     * @return
     * @throws java.lang.Exception
     */
    public Cancelable register(Strategy strategy) throws Exception {
        strategy.release();

        final TimeFrame timeFrame = strategy.timeFrame;
        final Instrument instrument = strategy.instrument;

        final TimeSeries timeSeries = new TimeSeries();
        strategy.setTimeSeries(timeSeries);
        strategiesTimeSeries.remove(strategy);
        strategiesTimeSeries.put(strategy, timeSeries);

        // Inicialização da estratégia
        strategy.initialize(getAccount());

        final List<Strategy> strategies = getStrategies(instrument, timeFrame);
        strategies.remove(strategy);
        strategies.add(strategy);

        // Pós inicialização de estratégias no Broker
        onRegister(strategy);

        return () -> {
            strategies.remove(strategy);
            strategiesTimeSeries.remove(strategy);
        };
    }

    /**
     * Invocado logo após registrar uma estratégia
     *
     * @param strategy
     * @throws Exception
     */
    protected void onRegister(Strategy strategy) throws Exception {
        final TimeFrame timeFrame = strategy.timeFrame;
        final Instrument instrument = strategy.instrument;
        // Carregar apenas os ultimos 30 candles,
        // Se já existirem registros, todos eles serão carregados
        Instant start = Instant.now().minusSeconds(timeFrame.seconds * 30);
        loadTimeSeries(instrument, timeFrame, start, Instant.now());
    }

    /**
     * Permite ao broker ser informado quando um novo candle é fechado para o instrumento e frame específico
     *
     * @param instrument
     * @param timeFrame
     * @param ohlc
     */
    protected void onData(Instrument instrument, TimeFrame timeFrame, OHLC ohlc) {
        if (!timeSeriesCached.containsKey(instrument)) {
            return;
        }

        final Map<TimeFrame, TimeSeries> timeSeriesByTimeframes = timeSeriesCached.get(instrument);
        if (!timeSeriesByTimeframes.containsKey(timeFrame)) {
            return;
        }

        final TimeSeries tsGlobal = timeSeriesByTimeframes.get(timeFrame);
        tsGlobal.add(ohlc);

        getStrategies(instrument, timeFrame).forEach((strategy) -> {
            final TimeSeries tsStrategy = strategiesTimeSeries.get(strategy);
            tsStrategy.add(ohlc);
            strategy.onData(ohlc);
        });

        // Após o processamento, salva o timeséries em disco, evita re-consultas ao broker
        final String timeSeriesName = getName() + "_" + instrument.getSymbol() + "_" + timeFrame.name();
        persistTimeseries(timeSeriesName, tsGlobal);
    }

    /**
     * Permite obter os dados de um instrumento para o período informado
     *
     * @param instrument
     * @param timeFrame
     * @param start
     * @param end
     * @throws java.lang.Exception
     */
    protected void loadTimeSeries(Instrument instrument, TimeFrame timeFrame, Instant start, Instant end) throws Exception {

        if (!timeSeriesCached.containsKey(instrument)) {
            timeSeriesCached.put(instrument, new ConcurrentHashMap<>());
        }

        final Map<TimeFrame, TimeSeries> timeSeriesByTimeframes = timeSeriesCached.get(instrument);
        if (!timeSeriesByTimeframes.containsKey(timeFrame)) {
            // Verifica se existe registro em disco, evita requisição desnecessária
            final String timeSeriesName = getName() + "_" + instrument.getSymbol() + "_" + timeFrame.name();
            final TimeSeries timeSeries = loadTimeseries(timeSeriesName);
            timeSeriesByTimeframes.put(timeFrame, timeSeries);
        }

        final TimeSeries timeSeries = timeSeriesByTimeframes.get(timeFrame);
        if (timeSeries.size() == 0) {
            //vazio
            List<OHLC> all = requestTimeSeries(instrument, timeFrame, start, end);
            timeSeries.add(all);
        }

        // Obtém valores falantes anteriores
        if (start.isBefore(Instant.ofEpochSecond(timeSeries.last().time))) {
            List<OHLC> before = requestTimeSeries(
                    instrument, timeFrame, start, Instant.ofEpochSecond(timeSeries.last().time)
            );
            timeSeries.add(before);
        }

        // Obtém valores falantes posteriores
        if (end.isAfter(Instant.ofEpochSecond(timeSeries.first().time))) {
            List<OHLC> after = requestTimeSeries(
                    instrument, timeFrame, Instant.ofEpochSecond(timeSeries.first().time), end
            );
            timeSeries.add(after);
        }

        // Atualiza também todos os timeseries usados nas estratégias
        final List<OHLC> ohlcs = timeSeries.ohlc(timeSeries.size());
        getStrategies(instrument, timeFrame)
                .stream()
                .filter((s) -> strategiesTimeSeries.containsKey(s))
                .map((s) -> strategiesTimeSeries.get(s))
                .forEach((t) -> t.add(ohlcs));
    }

    /**
     * Permite iterar nas estratégias registradas neste broker
     *
     * @param consumer
     */
    protected void forEachStrategies(TrheeConsumer<Strategy, Instrument, TimeFrame> consumer) {
        strategiesRegistered.forEach((instrument, tfStr) -> {
            tfStr.forEach((timeframe, strategies) -> {
                strategies.forEach((strategy) -> {
                    consumer.accept(strategy, instrument, timeframe);
                });
            });
        });
    }

    /**
     * Obtém a lista de Estratégias por instrumento e timeframe
     *
     * @param instrument
     * @param timeFrame
     * @return
     */
    private List<Strategy> getStrategies(final Instrument instrument, final TimeFrame timeFrame) {
        if (!strategiesRegistered.containsKey(instrument)) {
            strategiesRegistered.put(instrument, new ConcurrentHashMap<>());
        }
        final Map<TimeFrame, List<Strategy>> strategiesByTimeframes = strategiesRegistered.get(instrument);
        if (!strategiesByTimeframes.containsKey(timeFrame)) {
            strategiesByTimeframes.put(timeFrame, new CopyOnWriteArrayList<>());
        }
        return strategiesByTimeframes.get(timeFrame);
    }

    /**
     * Faz o carregamento de uma timeséries que está salva em disco, afim de evitar rechamadas ao servidores
     *
     * @param name
     * @param timeSeries
     */
    private TimeSeries loadTimeseries(String name) {
        TimeSeries out = new TimeSeries();
        File dir = new File(System.getProperty("java.io.tmpdir") + "/ta-timeseries/");
        if (!dir.exists()) {
            return out;
        }
        final Path path = dir.toPath().resolve(name);
        if (!Files.exists(path)) {
            return out;
        }

        try (Stream<String> stream = Files.lines(path)) {
            final List<String> lines = stream.collect(Collectors.toList());
            for (int i = 0, j = lines.size(); i < j; i++) {
                String[] parts = lines.get(i).trim().split(", ");
                Number[] vals = new Number[parts.length];

                for (int k = 0, l = parts.length; k < l; k++) {
                    String value = parts[k].trim();
                    if (k == 0) {
                        vals[0] = Long.valueOf(value);
                    } else {
                        vals[k] = Double.valueOf(value);
                    }
                }

                final OHLC ohlc = new OHLC(
                        (long) vals[0],
                        (double) vals[1],
                        (double) vals[2],
                        (double) vals[3],
                        (double) vals[4]
                );
                out.add(ohlc);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return out;
    }

    /**
     * Persiste am disco um timeséries.
     *
     * Invocado após o processamento de novos dados
     *
     * @param name
     * @param timeSeries
     */
    private void persistTimeseries(String name, TimeSeries timeSeries) {
        File dir = new File(System.getProperty("java.io.tmpdir") + "/ta-timeseries");
        if (!dir.exists()) {
            dir.mkdir();
        }

        try (PrintWriter pw = new PrintWriter(dir.toPath().resolve(name).toFile())) {
            timeSeries.stream().forEach((t) -> {
                final OHLC ohlc = t.getValue();
                final String line = Arrays.toString(new Number[]{
                    ohlc.time, ohlc.open, ohlc.high, ohlc.low, ohlc.close
                });
                pw.println(line.substring(1, line.length() - 1));
            });
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

}
