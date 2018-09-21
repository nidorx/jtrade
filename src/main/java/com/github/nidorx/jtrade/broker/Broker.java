package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.core.InstrumentImpl;
import com.github.nidorx.jtrade.core.Instrument;
import com.github.nidorx.jtrade.broker.trading.Position;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.util.Cancelable;
import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.Strategy;
import com.github.nidorx.jtrade.core.Tick;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representação de um Broker
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public abstract class Broker {

    /**
     * A última data conhecida do server.
     *
     * Usado nas estratégias e validações temporais.
     *
     * Importante usar esta informação para garantir a integridade das estratégias
     */
    private Instant serverTime = Instant.EPOCH;

    /**
     * A lista de instrumentos deste Broker
     */
    private final Map<String, Instrument> instruments = new ConcurrentHashMap<>();

    /**
     * Lista de estratégias registradas por instrumento e timeframe
     */
    private final Map<Instrument, Strategy> strategies = new ConcurrentHashMap<>();

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
     * @param symbol
     * @return
     * @throws java.lang.Exception
     */
    public abstract Account getAccountSummary(String symbol) throws Exception;

    /**
     * Obtém a posição aberta (se disponível) para o simbolo informado
     *
     * @param instrument
     * @return
     * @throws java.lang.Exception
     */
    public abstract Position getPosition(Instrument instrument) throws Exception;

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

    public abstract Order sellLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public abstract Order sellStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public abstract void modify(Order order, double price, double volume, double sl, double tp) throws TradeException;

    public abstract void remove(Order order) throws TradeException;

    public abstract void modify(Position position, double sl, double tp) throws TradeException;

    public abstract void close(Position position, double price, long deviation) throws TradeException;

    public abstract void closePartial(Position position, double price, double volume, long deviation) throws TradeException;

    /**
     * Market Execution
     *
     * @param instrument
     * @param volume
     * @return
     * @throws TradeException
     */
    public Order buy(Instrument instrument, double volume) throws TradeException {
        return buy(instrument, instrument.ask(), volume, 0);
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
     * Market Execution
     *
     * @param instrument
     * @param volume
     * @return
     * @throws TradeException
     */
    public Order sell(Instrument instrument, double volume) throws TradeException {
        return sell(instrument, instrument.bid(), volume, 0);
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

    public Order sellLimit(Instrument instrument, double price, double volume) throws TradeException {
        return sellLimit(instrument, price, volume, 0, 0);
    }

    public Order buyStop(Instrument instrument, double price, double volume) throws TradeException {
        return buyStop(instrument, price, volume, 0, 0);
    }

    public abstract Order buyStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException;

    public Order sellStop(Instrument instrument, double price, double volume) throws TradeException {
        return sellStop(instrument, price, volume, 0, 0);
    }

    public void modify(Order order, double price, double volume) throws TradeException {
        modify(order, price, volume, 0, 0);
    }

    public void modifyStop(Order order, double sl, double tp) throws TradeException {
        modify(order, 0, 0, sl, tp);
    }

    /**
     * Obtém a última hora conhecida do servidor.
     *
     * @return
     */
    public Instant getServerTime() {
        return serverTime;
    }

    /**
     * Obtém um instrumento a partir do símbolo informado
     *
     * @param symbol
     * @return
     * @throws Exception
     */
    public Instrument getInstrument(final String symbol) throws Exception {
        return instruments.get(symbol);
    }

    /**
     * Obtém a lista de instrumentos negociáveis por este broker
     *
     * @return
     * @throws Exception
     */
    public List<Instrument> getInstruments() throws Exception {
        return new ArrayList<>(instruments.values());
    }

    /**
     * Obtém a estratégia registrada para o instrumento
     *
     * @param symbol
     * @return
     * @throws java.lang.Exception
     */
    public Strategy getStrategy(final String symbol) throws Exception {
        final Instrument instrument = getInstrument(symbol);
        if (instrument == null) {
            return null;
        }
        return strategies.get(instrument);
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

    /**
     * Registra uma estratégia para ser executada neste contexto.
     *
     * A estratégia passará a receber atualizações do contexto e executar transações no Broker deste contexto
     *
     * Só é permitido uma única estratégia para um Instrumento, afim de evitar lógicas conflitantes
     *
     * @param strategy
     * @param symbol
     * @return
     * @throws java.lang.Exception
     */
    public Cancelable register(final Strategy strategy, final String symbol) throws Exception {

        final Instrument instrument = getInstrument(symbol);
        if (getStrategy(symbol) != null) {
            throw new Exception("A strategy is already registered for the symbol:" + symbol);
        }

        // Adiciona na listagem
        strategies.put(instrument, strategy);

        // Inicialização da estratégia
        strategy.registerOn(this, symbol);
        strategy.initialize(getAccount());

        return () -> {
            if (strategies.containsKey(instrument)) {
                strategies.get(instrument).release();
                strategies.remove(instrument);
            }
        };
    }

    /**
     * Permite ao broker ser informado quando um novo candle é fechado para o instrumento e frame específico
     *
     * @param tick
     */
    protected void processTick(Tick tick) {

        try {
            // Atualiza a data conhecida do servidor
            if (tick.time.isAfter(serverTime)) {
                serverTime = tick.time;
            }

            final InstrumentImpl instrument = (InstrumentImpl) getInstrument(tick.symbol);

            if (instrument != null) {

                // Informa ao instrumento sobre o novo tick
                instrument.processTick(tick);

                final Strategy strategy = getStrategy(tick.symbol);
                if (strategy != null) {
                    // Informa à estratégia
                    strategy.processTick(tick);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Broker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Permite ao broker ser informado quando um novo candle é fechado para o instrumento e frame específico
     *
     * @param rate
     * @throws java.lang.Exception
     */
    protected void processRate(Rate rate) throws Exception {

        final InstrumentImpl instrument = (InstrumentImpl) getInstrument(rate.symbol);
        if (instrument != null) {

            // Informa ao instrumento
            instrument.processRate(rate);

            final Strategy strategy = getStrategy(rate.symbol);
            if (strategy != null) {
                // Informa à estratégia
                strategy.onRate(rate);
            }
        }
    }

    /**
     * Permite ao broker registrar os instrumentos que ele gerencia
     *
     * @param symbol
     * @param base
     * @param quote
     * @throws Exception
     */
    protected void createInstrument(String symbol, Currency base, Currency quote) throws Exception {
        final InstrumentImpl instrument = (InstrumentImpl) getInstrument(symbol);
        if (instrument != null) {
            throw new Exception("A instrument is already registered for the symbol:" + symbol);
        }
        instruments.put(symbol, new InstrumentImpl(symbol, base, quote));
    }

    /**
     * Permite ao broker registrar os instrumentos que ele gerencia
     *
     * @param symbol
     * @param base
     * @param quote
     * @param digits
     * @param contractSize
     * @param tickValue
     * @throws Exception
     */
    protected void createInstrument(String symbol, Currency base, Currency quote, int digits, int contractSize, double tickValue) throws Exception {
        final InstrumentImpl instrument = (InstrumentImpl) getInstrument(symbol);
        if (instrument != null) {
            throw new Exception("A instrument is already registered for the symbol:" + symbol);
        }
        instruments.put(symbol, new InstrumentImpl(symbol, base, quote, digits, contractSize, tickValue));
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
//    protected void loadTimeSeries(Instrument instrument, TimeFrame timeFrame, Instant start, Instant end) throws Exception {
//
//        if (!timeSeriesCached.containsKey(instrument)) {
//            timeSeriesCached.put(instrument, new ConcurrentHashMap<>());
//        }
//
//        final Map<TimeFrame, TimeSeries> timeSeriesByTimeframes = timeSeriesCached.get(instrument);
//        if (!timeSeriesByTimeframes.containsKey(timeFrame)) {
//            // Verifica se existe registro em disco, evita requisição desnecessária
//            final String timeSeriesName = getName() + "_" + instrument.getSymbol() + "_" + timeFrame.name();
//            final TimeSeries timeSeries = loadTimeseries(timeSeriesName);
//            timeSeriesByTimeframes.put(timeFrame, timeSeries);
//        }
//
//        final TimeSeries timeSeries = timeSeriesByTimeframes.get(timeFrame);
//        if (timeSeries.size() == 0) {
//            //vazio
//            List<OHLC> all = requestTimeSeries(instrument, timeFrame, start, end);
//            timeSeries.add(all);
//        }
//
//        // Obtém valores falantes anteriores
//        if (start.isBefore(Instant.ofEpochSecond(timeSeries.last().time))) {
//            List<OHLC> before = requestTimeSeries(
//                    instrument, timeFrame, start, Instant.ofEpochSecond(timeSeries.last().time)
//            );
//            timeSeries.add(before);
//        }
//
//        // Obtém valores falantes posteriores
//        if (end.isAfter(Instant.ofEpochSecond(timeSeries.first().time))) {
//            List<OHLC> after = requestTimeSeries(
//                    instrument, timeFrame, Instant.ofEpochSecond(timeSeries.first().time), end
//            );
//            timeSeries.add(after);
//        }
//
//        // Atualiza também todos os timeseries usados nas estratégias
//        final List<OHLC> ohlcs = timeSeries.ohlc(timeSeries.size());
////        getStrategies(instrument, timeFrame);
////                .stream()
////                .filter((s) -> strategiesTimeSeries.containsKey(s))
////                .map((s) -> strategiesTimeSeries.get(s))
////                .forEach((t) -> t.add(ohlcs));
//    }
    /**
     * Faz o carregamento de uma timeséries que está salva em disco, afim de evitar rechamadas ao servidores
     *
     * @param name
     * @param timeSeries
     */
//    private TimeSeries loadTimeseries(String name) {
//        TimeSeries out = new TimeSeries();
//        File dir = new File(System.getProperty("java.io.tmpdir") + "/ta-timeseries/");
//        if (!dir.exists()) {
//            return out;
//        }
//        final Path path = dir.toPath().resolve(name);
//        if (!Files.exists(path)) {
//            return out;
//        }
//
//        try (Stream<String> stream = Files.lines(path)) {
//            final List<String> lines = stream.collect(Collectors.toList());
//            for (int i = 0, j = lines.size(); i < j; i++) {
//                String[] parts = lines.get(i).trim().split(", ");
//                Number[] vals = new Number[parts.length];
//
//                for (int k = 0, l = parts.length; k < l; k++) {
//                    String value = parts[k].trim();
//                    if (k == 0) {
//                        vals[0] = Long.valueOf(value);
//                    } else {
//                        vals[k] = Double.valueOf(value);
//                    }
//                }
//
//                final OHLC ohlc = new OHLC(
//                        (long) vals[0],
//                        (double) vals[1],
//                        (double) vals[2],
//                        (double) vals[3],
//                        (double) vals[4]
//                );
//                out.add(ohlc);
//            }
//        } catch (IOException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
//        return out;
//    }
    /**
     * Persiste am disco um timeséries.
     *
     * Invocado após o processamento de novos dados
     *
     * @param name
     * @param timeSeries
     */
//    private void persistTimeseries(String name, TimeSeries timeSeries) {
//        File dir = new File(System.getProperty("java.io.tmpdir") + "/ta-timeseries");
//        if (!dir.exists()) {
//            dir.mkdir();
//        }
//
//        try (PrintWriter pw = new PrintWriter(dir.toPath().resolve(name).toFile())) {
//            timeSeries.stream().forEach((t) -> {
//                final OHLC ohlc = t.getValue();
//                final String line = Arrays.toString(new Number[]{
//                    ohlc.time, ohlc.open, ohlc.high, ohlc.low, ohlc.close
//                });
//                pw.println(line.substring(1, line.length() - 1));
//            });
//        } catch (FileNotFoundException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
//    }
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

}
