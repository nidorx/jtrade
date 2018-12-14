package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.core.Account;
import com.github.nidorx.jtrade.core.impl.InstrumentImpl;
import com.github.nidorx.jtrade.core.Instrument;
import com.github.nidorx.jtrade.broker.trading.Position;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.util.function.Cancelable;
import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.Strategy;
import com.github.nidorx.jtrade.core.Tick;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import java.time.Instant;
import java.util.ArrayList;
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
     * Importante usar esta informação para garantir a integridade das
     * estratégias
     */
    private Instant serverTime = Instant.EPOCH;

    /**
     * Informações atualizadas sobre a conta de operação
     */
    private Account account;

    /**
     * A lista de instrumentos deste Broker
     */
    private final Map<String, Instrument> instruments = new ConcurrentHashMap<>();

    /**
     * Lista de estratégias registradas por instrumento e timeframe
     */
    private final Map<Instrument, Strategy> strategies = new ConcurrentHashMap<>();

    /**
     * As posições abertas por instrumento
     */
    private final Map<Instrument, List<Position>> positions = new ConcurrentHashMap<>();

    /**
     * As ordens abertas por instrumento não associadas a uma posição
     */
    private final Map<Instrument, List<Order>> orders = new ConcurrentHashMap<>();

    /**
     * Obtém o nome único do Broker, usado para persistir os TimeSeries em disco
     *
     * @return
     */
    public abstract String getName();

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @param sl
     * @param tp
     * @throws TradeException
     */
    public abstract void buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException;

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @param sl
     * @param tp
     * @throws TradeException
     */
    public abstract void sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException;

    public abstract void remove(Order order) throws TradeException;

    public abstract void close(Position position, double price, long deviation) throws TradeException;

    /**
     * Market Execution
     *
     * @param instrument
     * @param volume
     * @throws TradeException
     */
    public final void buy(Instrument instrument, double volume) throws TradeException {
        buy(instrument, instrument.ask(), volume, 0);
    }

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @throws TradeException
     */
    public final void buy(Instrument instrument, double price, double volume, long deviation) throws TradeException {
        buy(instrument, price, volume, deviation, 0, 0);
    }

    /**
     * Market Execution
     *
     * @param instrument
     * @param volume
     * @throws TradeException
     */
    public final void sell(Instrument instrument, double volume) throws TradeException {
        sell(instrument, instrument.bid(), volume, 0);
    }

    /**
     * Instant Execution
     *
     * @param instrument
     * @param price
     * @param volume
     * @param deviation
     * @throws TradeException
     */
    public final void sell(Instrument instrument, double price, double volume, long deviation) throws TradeException {
        sell(instrument, price, volume, deviation, 0, 0);
    }

    /**
     * Obtém as informações atualizadas da conta
     *
     * @return
     */
    public final Account getAccount() {
        return account;
    }

    /**
     * Obtém a última hora conhecida do servidor.
     *
     * @return
     */
    public final Instant getServerTime() {
        return serverTime;
    }

    /**
     * Obtém um instrumento a partir do símbolo informado
     *
     * @param symbol
     * @return
     * @throws Exception
     */
    public final Instrument getInstrument(final String symbol) throws Exception {
        return instruments.get(symbol);
    }

    /**
     * Obtém a lista de instrumentos negociáveis por este broker
     *
     * @return
     * @throws Exception
     */
    public final List<Instrument> getInstruments() throws Exception {
        return new ArrayList<>(instruments.values());
    }

    /**
     * Obtém as posições abertas (se disponível) para o simbolo informado
     *
     * @param instrument
     * @return
     */
    public final List<Position> getPositions(Instrument instrument) {
        if (!positions.containsKey(instrument)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(positions.get(instrument));
    }

    /**
     * Obtém as ordens abertas para o símbolo
     *
     * As ordens abertas não estão associadas a uma posição ainda
     *
     * @param instrument
     * @return
     */
    public final List<Order> getOrders(Instrument instrument) {
        if (!orders.containsKey(instrument)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(orders.get(instrument));
    }

    /**
     * Obtém a estratégia registrada para o instrumento
     *
     * @param symbol
     * @return
     * @throws java.lang.Exception
     */
    public final Strategy getStrategy(final String symbol) throws Exception {
        final Instrument instrument = getInstrument(symbol);
        if (instrument == null) {
            return null;
        }
        return strategies.get(instrument);
    }

    /**
     * Registra uma estratégia para ser executada neste contexto.
     *
     * A estratégia passará a receber atualizações do contexto e executar
     * transações no Broker deste contexto
     *
     * Só é permitido uma única estratégia para um Instrumento, afim de evitar
     * lógicas conflitantes
     *
     * @param strategy
     * @param symbol
     * @return
     * @throws java.lang.Exception
     */
    public Cancelable register(final Strategy strategy, final String symbol) throws Exception {

        final Instrument instrument = getInstrument(symbol);

        final Cancelable cancelable = () -> {
            if (strategies.containsKey(instrument)) {
                strategies.get(instrument).release();
                strategies.remove(instrument);
            }
        };

        final Strategy registered = getStrategy(symbol);
        if (registered != null) {
            if (registered.equals(strategy)) {
                return cancelable;
            }
            throw new Exception("A strategy is already registered for the symbol:" + symbol);
        }

        // Adiciona na listagem
        strategies.put(instrument, strategy);

        // Inicialização da estratégia
        strategy.registerOn(this, symbol);
        strategy.initialize(getAccount());

        return cancelable;
    }

    /**
     * Permite definir os detalhes da conta atual
     *
     * @param account
     */
    protected final void setAccount(Account account) {
        if (account.time.isBefore(getServerTime())) {
            // Informação defazada sobre o status da conta
            return;
        }

        if (account.time.isAfter(getServerTime())) {
            // Atualiza o server time, se necessário
            setServerTime(account.time);
        }

        this.account = account;
    }

    /**
     * Define última hora conhecida do servidor.
     *
     * @param instant
     */
    protected final void setServerTime(Instant instant) {
        serverTime = instant;
    }

    /**
     * Permite definir as posições abertas
     *
     * @param instrument
     * @param newPositions
     */
    protected final void setPositions(Instrument instrument, final List<Position> newPositions) {
        if (newPositions == null || newPositions.isEmpty()) {
            if (this.positions.containsKey(instrument)) {
                this.positions.remove(instrument);
            }
        } else {
            this.positions.put(instrument, new ArrayList<>(newPositions));
        }
    }

    /**
     * Permite definir as ordens abertas que não possuem posição
     *
     * @param instrument
     * @param newOrders
     */
    protected final void setOrders(Instrument instrument, final List<Order> newOrders) {
        if (newOrders == null || newOrders.isEmpty()) {
            if (this.orders.containsKey(instrument)) {
                this.orders.remove(instrument);
            }
        } else {
            this.orders.put(instrument, new ArrayList<>(newOrders));
        }
    }

    /**
     * Permite ao broker ser informado quando um novo candle é fechado para o
     * instrumento e frame específico
     *
     * @param tick
     */
    protected final void processTick(Tick tick) {

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
     * Permite ao broker ser informado quando um novo candle é fechado para o
     * instrumento e frame específico
     *
     * @param rate
     * @throws java.lang.Exception
     */
    protected final void processRate(Rate rate) throws Exception {

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
    protected final void createInstrument(String symbol, String base, String quote) throws Exception {
        final InstrumentImpl instrument = (InstrumentImpl) getInstrument(symbol);
        if (instrument != null) {
            throw new Exception("A instrument is already registered for the symbol:" + symbol);
        }
        instruments.put(symbol, new InstrumentImpl(symbol, base, quote));
    }

    protected final void createInstrument(String symbol, String base, String quote, int digits, double contractSize,
            double tickValue, double bid, double ask) throws Exception {
        final InstrumentImpl instrument = (InstrumentImpl) getInstrument(symbol);
        if (instrument != null) {
            throw new Exception("A instrument is already registered for the symbol:" + symbol);
        }
        instruments.put(symbol, new InstrumentImpl(symbol, base, quote, digits, contractSize, tickValue, bid, ask));
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
     * Faz o carregamento de uma timeséries que está salva em disco, afim de
     * evitar rechamadas ao servidores
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
    public final double exchangeRate(String base, String quoted) throws Exception {
        String accCurrency = getAccount().currency;

        if (accCurrency.equals(base)) {
            // Ex. acc=USD, base = USD, quoted = JPY (USDJPY)
            return getInstrument(base + quoted).bid();
        } else if (accCurrency.equals(quoted)) {
            // Ex. acc=USD, base = EUR, quoted = USD (EURUSD)
            return 1;
        } else {
            Instrument instrument = getInstrument(accCurrency + quoted);
            if (instrument != null) {
                return instrument.bid();
            }
            instrument = getInstrument(quoted + accCurrency);
            if (instrument != null) {
                return 1 / instrument.bid();
            }
        }

        return 1;
    }

    public final double exchange(double value, String from, String to) throws Exception {
        return value * exchangeRate(from, to);
    }

}
