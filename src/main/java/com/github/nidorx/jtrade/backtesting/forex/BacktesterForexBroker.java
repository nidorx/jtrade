package com.github.nidorx.jtrade.backtesting.forex;

import com.github.nidorx.jtrade.util.Util;
import com.github.nidorx.jtrade.OHLC;
import com.github.nidorx.jtrade.Strategy;
import com.github.nidorx.jtrade.TimeFrame;
import com.github.nidorx.jtrade.backtesting.BacktesterBroker;
import com.github.nidorx.jtrade.broker.Account;
import com.github.nidorx.jtrade.broker.trading.Deal;
import com.github.nidorx.jtrade.broker.trading.Order;
import com.github.nidorx.jtrade.broker.trading.OrderState;
import com.github.nidorx.jtrade.broker.trading.OrderType;
import com.github.nidorx.jtrade.broker.trading.Position;
import com.github.nidorx.jtrade.broker.trading.DealEntry;
import com.github.nidorx.jtrade.broker.trading.DealType;
import com.github.nidorx.jtrade.broker.trading.PositionType;
import com.github.nidorx.jtrade.broker.exception.TradeException;
import com.github.nidorx.jtrade.broker.exception.TradeExceptionReason;
import com.github.nidorx.jtrade.Instrument;
import com.github.nidorx.jtrade.util.GoogleFinanceAPI;
import com.github.nidorx.jtrade.util.SimpleCache;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uma implementação de Broker genérico para BackTest.
 *
 * A busca de dados é feito utilizando a api do Google Finance, o gerenciamento de posições e ordens é em memória
 *
 * https://www.mql5.com/en/docs/constants/structures/mqltraderequest https://book.mql4.com/appendix/limits
 * https://book.mql4.com/trading/orders
 *
 * @author Alex
 */
public class BacktesterForexBroker extends BacktesterBroker {

    private static final AtomicLong SEQ = new AtomicLong();

    private static final SimpleCache<Double> DOUBLE_CACHE = new SimpleCache<>();

    /**
     * O resumo atual da conta
     */
    private Account summary;

    /**
     * A posição aberta atualmente, se existir
     */
    private Position position;

    /**
     * Instante inicial do periodo de testes
     */
    private Instant start;

    /**
     * Instante final do periodo de testes
     */
    private Instant end;

    /**
     * Mantém o histórico de movimentação da conta, usado na geração de dados estatísticos
     */
    private final List<Account> accountHistory = new ArrayList<>();

    /**
     * Lista de instrumentos deste broker
     */
    private final Map<String, Instrument> instruments = new HashMap<>();

    /**
     * Listagem das ordens ativas e pendentes
     */
    private final List<Order> orders = new ArrayList<>();

    @Override
    public String getName() {
        return "BacktesterForexBroker";
    }

    @Override
    public void initialize(Account account, Instant start, Instant end) throws Exception {
        this.summary = account;
        this.start = start;
        this.end = end;

    }

    @Override
    public boolean nextTick() throws Exception {

        forEachStrategies((strategy, instrument, timeframe) -> {
            // @TODO: Verificar se a execução da estratégia já foi concluída
            // 1) Obtér o proximo OHLC
            // ---------------------------------------------------------------------------------------------------------

            // O tick anterior
            final OHLC lastTick = rates(instrument, timeframe);

            // @TODO: O novo tick
            final OHLC tick = rates(instrument, timeframe);

            try {
                List<Order> orders = getOrders(instrument);
                // 2) Validar as ordens pendentes
                // -----------------------------------------------------------------------------------------------------
                for (int i = 0; i < orders.size(); i++) {
                    Order order = orders.get(i);
                    if (order.getState().equals(OrderState.PLACED)) {
                        // Verifica se o valor da ordem está entre o mínimo e máximo do novo tick

                        // @TODO: Gerar um ask bid proporcional ao novo tick, é um askbid que aconteceu no intervalo
                        // do novo tick
                        double ask = Util.between(tick.low, tick.high);
                        double bid = Util.between(tick.low, tick.high);

                        // Transforming a Pending Order into a Market Order
                        switch (order.getType()) {
                            // Ask price reaches open price
                            case BUY_LIMIT:
                                if (order.getPrice() >= ask) {
                                    fillValidOrder(order);
                                }
                                break;
                            case BUY_STOP:
                                break;
                            // Bid price reaches open price
                            case SELL_LIMIT:
                            case SELL_STOP:
                                if (order.getPrice() <= bid) {
                                    // Verifica execução da ordem
                                }
                                break;
                        }
                    }
                }

                // 3) Validar a posição aberta
                // -----------------------------------------------------------------------------------------------------
                // 4) Atualizar a conta do usuario (Margem, etc)
                // -----------------------------------------------------------------------------------------------------
                // 5) Atualizar o ask e bid
                // -----------------------------------------------------------------------------------------------------
                // 6) Invocar o método onData (consequentemente a estratégia será invocada)
                // -----------------------------------------------------------------------------------------------------
                onData(instrument, timeframe, tick);

                // 7) Possui mais ticks para validar?
                // -----------------------------------------------------------------------------------------------------
            } catch (Exception ex) {
                Logger.getLogger(BacktesterForexBroker.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        return false;
    }

    @Override
    public Account getAccount() {
        return summary;
    }

    @Override
    public Account getAccountSummary(Instrument instrument) {
        return summary;
    }

    @Override
    protected List<OHLC> requestTimeSeries(Instrument instrument, TimeFrame timeFrame, Instant start, Instant end) throws Exception {

        final List<OHLC> timeSeries = new ArrayList();
        long days = TimeUnit.SECONDS.toDays(end.getEpochSecond() - start.getEpochSecond());

        GoogleFinanceAPI.get(
                "CURRENCY",
                instrument.getSymbol(),
                GoogleFinanceAPI.Period.DAY.last((int) days).interval(timeFrame)
        ).entrySet().stream().forEach((t) -> {
            Instant instant = t.getKey();
            double[] val = t.getValue();
            timeSeries.add(new OHLC(t.getKey().getEpochSecond(), val[0], val[1], val[2], val[3]));
        });

        return timeSeries;
    }

    /**
     * Invocado logo após registrar uma estratégia
     *
     * @param strategy
     * @throws Exception
     */
    @Override
    protected void onRegister(Strategy strategy) throws Exception {
        final TimeFrame timeFrame = strategy.timeFrame;
        final Instrument instrument = strategy.instrument;
        // Carregar apenas os primeiros 30 candles,
        // Se já existirem registros, todos eles serão carregados
        loadTimeSeries(instrument, timeFrame, start, start.plusSeconds(timeFrame.seconds * 30));
    }

    @Override
    public Position getPosition(Instrument instrument) throws Exception {
        return position;
    }

    @Override
    public List<Instrument> getInstruments() throws Exception {
        if (instruments.isEmpty()) {

            // Inicializa a lista de instrumentos
            instruments.put(
                    "EURUSD",
                    new Instrument(this, "EURUSD", Currency.getInstance("EUR"), Currency.getInstance("USD"), 5, 100000, 1.0)
            );

            instruments.put(
                    "USDJPY",
                    new Instrument(this, "USDJPY", Currency.getInstance("USD"), Currency.getInstance("JPY"), 3, 100000, 1.0)
            );
        }
        return new ArrayList<>(instruments.values());
    }

    @Override
    public Instrument getInstrument(String symbol) throws Exception {
        // @TODO:
        return null;
    }

    @Override
    public double bid(Instrument instrument) {
        return DOUBLE_CACHE.get("bid_" + instrument.getSymbol(), () -> {
            for (TimeFrame timeFrame : TimeFrame.all()) {
                OHLC tick = rates(instrument, timeFrame);
                if (tick != null) {
                    // Número aleatório entre o fechamento anterior decrementado de 0 a -10% do tamanho do corpo do tick
                    double spread = Math.abs(tick.open - tick.close) * 0.1;
                    return tick.close - Util.between(0, spread);
                }
            }
            return 0;
        }, 10);

    }

    @Override
    public double ask(Instrument instrument) {
        return DOUBLE_CACHE.get("bid_" + instrument.getSymbol(), () -> {
            for (TimeFrame timeFrame : TimeFrame.all()) {
                OHLC tick = rates(instrument, timeFrame);
                if (tick != null) {
                    // Número aleatório entre o fechamento anterior acrescido de 0 a -10% do tamanho do corpo do tick
                    double spread = Math.abs(tick.open - tick.close) * 0.1;
                    return tick.close + Util.between(0, spread);
                }
            }
            return 0;
        }, 10);
    }

    @Override
    public double stopLevel(Instrument instrument) {
        return DOUBLE_CACHE.get("stopLevel_" + instrument.getSymbol(), () -> {
            return instrument.ceil(Math.max(spread(instrument), Util.between(0, 5)));
        }, 20);
    }

    @Override
    public double freezeLevel(Instrument instrument) {
        return DOUBLE_CACHE.get("freezeLevel_" + instrument.getSymbol(), () -> {
            return Util.between(1, 15);
        }, 20);
    }

    /**
     * Insere um Ordem válida na posição aberta ou cria a posição para a ordem
     *
     * @param order
     * @throws TradeException
     */
    private void fillValidOrder(final Order order) throws TradeException {

        final Instrument instrument = order.getInstrument();
        final OHLC tick = rates(instrument);
        final Instant instant = Instant.ofEpochSecond(tick.time + 5);
        final double ask = ask(instrument);
        final double bid = bid(instrument);
        final double volume = order.getVolume();

        // @TODO: Verificar a margem
        // https://www.metatrader5.com/en/terminal/help/trading_advanced/margin_forex
        // https://www.metatrader5.com/en/terminal/help/trading_advanced/margin_exchange
        double margin = ForexUtils.orderMargin(volume, ask, summary.getLeverage(), instrument.getContractSize());
        if (summary.getMargin() < margin) {
            throw new TradeException(TradeExceptionReason.NO_MONEY);
        }

        // Posição já está fechada
        if (position != null && position.volume() == 0) {
            position = null;
        }

        // Não existe posição aberta
        if (position == null) {
            position = new Position(
                    this,
                    SEQ.incrementAndGet(),
                    order.getType().isBuy() ? PositionType.BUY : PositionType.SELL, instrument,
                    order.getType().isBuy() ? ask : bid,
                    order.getStopLoss(),
                    order.getTakeProfit()
            );
        }

        if (order.getType().isBuy()) {
            if (position.getType().equals(PositionType.BUY)) {
                // Incremento de compra na posição aberta
                final Deal deal = new Deal(SEQ.incrementAndGet(), order.getId(), instant, DealType.BUY, DealEntry.IN, ask, volume, 0.0, 0.0, 0.0);
                order.setState(OrderState.FILLED);
                order.addDeal(deal);
                position.addOrder(order);
            } else {
                //---------------------------------------------------------------------------------
                // PositionType.SELL
                // Posição aberta em sentido oposto
                //---------------------------------------------------------------------------------
                final double volumePos = position.volume();
                if (volumePos > volume) {
                    // SAÍDA PARCIAL
                    final Deal deal = new Deal(SEQ.incrementAndGet(), order.getId(), instant, DealType.BUY, DealEntry.OUT, ask, volume, 0.0, 0.0, 0.0);
                    order.setState(OrderState.FILLED);
                    order.addDeal(deal);
                    position.addOrder(order);
                } else {
                    // @TODO: DealEntry.OUT ou DealEntry.OUTby?
                    final Deal deal = new Deal(SEQ.incrementAndGet(), order.getId(), instant, DealType.BUY, DealEntry.OUT, ask, volumePos, 0.0, 0.0, 0.0);
                    order.setState(OrderState.FILLED);
                    order.addDeal(deal);
                    position.addOrder(order);
                    position = null;

                    if (volume >= volumePos) {
                        // FAZ NOVA COMPRA
                        // Se o volume solicitado foi maior do que o necessário para fechar a posicao,
                        // Executa nova ordem com o volume restante
                        double diff = volume - volumePos;
                        buy(instrument, ask, diff, 0);
                    }
                }
            }
        } else {
            if (position.getType().equals(PositionType.SELL)) {
                // Incremento de compra na posição aberta
                final Deal deal = new Deal(SEQ.incrementAndGet(), order.getId(), instant, DealType.SELL, DealEntry.IN, bid, volume, 0.0, 0.0, 0.0);
                order.setState(OrderState.FILLED);
                order.addDeal(deal);
                position.addOrder(order);
            } else {
                //---------------------------------------------------------------------------------
                // PositionType.SELL
                // Posição aberta em sentido oposto
                //---------------------------------------------------------------------------------
                final double volumePos = position.volume();
                if (volumePos > volume) {
                    // SAÍDA PARCIAL
                    final Deal deal = new Deal(SEQ.incrementAndGet(), order.getId(), instant, DealType.SELL, DealEntry.OUT, bid, volume, 0.0, 0.0, 0.0);
                    order.setState(OrderState.FILLED);
                    order.addDeal(deal);
                    position.addOrder(order);
                    // @TODO: OrderFilling
                } else {
                    // FECHA A POSIÇÃO
                    // @TODO: DealEntry.OUT ou DealEntry.OUTby?
                    final Deal deal = new Deal(SEQ.incrementAndGet(), order.getId(), instant, DealType.SELL, DealEntry.OUT, bid, volumePos, 0.0, 0.0, 0.0);
                    order.setState(OrderState.FILLED);
                    order.addDeal(deal);
                    position.addOrder(order);
                    position = null;

                    if (volume >= volumePos) {
                        // FAZ NOVA COMPRA
                        // Se o volume solicitado foi maior do que o necessário para fechar a posicao,
                        // Executa nova ordem com o volume restante
                        double diff = volume - volumePos;
                        sell(instrument, bid, diff, 0);
                    }
                }
            }
        }
    }

    @Override
    public Order buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        double ask = ask(instrument);
        double bid = bid(instrument);
        checkPriceVolume(price, volume);
        checkBuyPriceDeviation(ask, price, deviation);

        // The limitation related to the position of stop levels of the market order to be opened is calculated on the 
        // basis of the correct market price used for closing the order.
        // Orders StopLoss and TakeProfit cannot be placed closer to the market price than the minimum distance.
        // https://book.mql4.com/trading/orders
        // https://book.mql4.com/appendix/limits
        double stopLevel = instrument.pointsToPrice(instrument.stopLevel());
        double stopLosssLevel = bid - stopLevel;
        double takeProfitLevel = bid + stopLevel;
        if (sl > 0 && sl > stopLosssLevel) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }
        if (tp > 0 && tp < takeProfitLevel) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }

        final Order order = new Order(this, SEQ.incrementAndGet(), OrderType.BUY, instrument, ask, volume);
        fillValidOrder(order);
        orders.add(order);
        return order;
    }

    @Override
    public Order sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {

        double ask = ask(instrument);
        double bid = bid(instrument);
        checkPriceVolume(price, volume);
        checkSellPriceDeviation(bid, price, deviation);

        // The limitation related to the position of stop levels of the market order to be opened is calculated on the 
        // basis of the correct market price used for closing the order.
        // Orders StopLoss and TakeProfit cannot be placed closer to the market price than the minimum distance.
        // https://book.mql4.com/trading/orders
        // https://book.mql4.com/appendix/limits        
        double stopLevel = instrument.pointsToPrice(instrument.stopLevel());
        double stopLosssLevel = ask + stopLevel;
        double takeProfitLevel = ask - stopLevel;
        if (sl > 0 && sl < stopLosssLevel) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }
        if (tp > 0 && tp > takeProfitLevel) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }

        final Order order = new Order(this, SEQ.incrementAndGet(), OrderType.SELL, instrument, bid, volume);
        fillValidOrder(order);
        orders.add(order);
        return order;
    }

    @Override
    public Order buyLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {

        double ask = instrument.ask();
        double stoplevel = instrument.stopLevel();
        double freezeLevel = instrument.freezeLevel();

        // Open Price of a Pending Order: Below the current Ask price
        if (!(price < ask)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        // ------------------------------------------
        // StopLevel Minimum Distance Limitation
        // ------------------------------------------
        // Open Price: Ask-OpenPrice ≥ StopLevel
        if (!(ask - price >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }
        // StopLoss (SL): OpenPrice-SL ≥ StopLevel
        if (sl > 0 && !(price - sl >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }
        // TakeProfit (TP): TP-OpenPrice ≥ StopLevel
        if (tp > 0 && !(tp - price >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }

        // ------------------------------------------
        // FreezeLevel Limitation (Freezing Distance)
        // ------------------------------------------
        // Open Price: 	Ask-OpenPrice > FreezeLevel
        if (!(ask - price > freezeLevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        final long id = SEQ.incrementAndGet();
        final Order order = new Order(this, id, OrderType.BUY_LIMIT, instrument, price, volume);
        order.setState(OrderState.PLACED);
        orders.add(order);
        return order;
    }

    @Override
    public Order sellLimit(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {

        double bid = instrument.ask();
        double stoplevel = instrument.stopLevel();
        double freezeLevel = instrument.freezeLevel();

        // Open Price of a Pending Order: Above the current Bid price
        if (!(price > bid)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        // ------------------------------------------
        // StopLevel Minimum Distance Limitation
        // ------------------------------------------
        // Open Price: OpenPrice-Bid ≥ StopLevel
        if (!(price - bid >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }
        // StopLoss (SL): SL-OpenPrice ≥StopLevel
        if (sl > 0 && !(sl - price >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }
        // TakeProfit (TP): OpenPrice-TP ≥ StopLevel
        if (tp > 0 && !(price - tp >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }

        // ------------------------------------------
        // FreezeLevel Limitation (Freezing Distance)
        // ------------------------------------------
        // Open Price: 	OpenPrice-Bid > FreezeLevel
        if (!(price - bid > freezeLevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        final long id = SEQ.incrementAndGet();
        final Order order = new Order(this, id, OrderType.SELL_LIMIT, instrument, price, volume);
        order.setState(OrderState.PLACED);
        orders.add(order);
        return order;
    }

    @Override
    public Order buyStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {

        double ask = instrument.ask();
        double stoplevel = instrument.stopLevel();
        double freezeLevel = instrument.freezeLevel();

        // Open Price of a Pending Order: Above the current Ask price
        if (!(price > ask)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        // ------------------------------------------
        // StopLevel Minimum Distance Limitation
        // ------------------------------------------
        // Open Price: 	OpenPrice-Ask ≥ StopLevel
        if (!(price - ask >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }
        // StopLoss (SL): OpenPrice-SL ≥ StopLevel
        if (sl > 0 && !(price - sl >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }
        // TakeProfit (TP): TP-OpenPrice ≥ StopLevel
        if (tp > 0 && !(tp - price >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }

        // ------------------------------------------
        // FreezeLevel Limitation (Freezing Distance)
        // ------------------------------------------
        // Open Price: 	OpenPrice-Ask > FreezeLevel	
        if (!(price - ask > freezeLevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        final long id = SEQ.incrementAndGet();
        final Order order = new Order(this, id, OrderType.BUY_STOP, instrument, price, volume);
        order.setState(OrderState.PLACED);
        orders.add(order);
        return order;
    }

    @Override
    public Order sellStop(Instrument instrument, double price, double volume, double sl, double tp) throws TradeException {

        double bid = instrument.ask();
        double stoplevel = instrument.stopLevel();
        double freezeLevel = instrument.freezeLevel();

        // Open Price of a Pending Order: Below the current Bid price
        if (!(price < bid)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        // ------------------------------------------
        // StopLevel Minimum Distance Limitation
        // ------------------------------------------
        // Open Price: Bid-OpenPrice ≥ StopLevel
        if (!(bid - price >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }
        // StopLoss (SL): SL-OpenPrice ≥ StopLevel
        if (sl > 0 && !(sl - price >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }
        // TakeProfit (TP): OpenPrice-TP ≥ StopLevel
        if (tp > 0 && !(price - tp >= stoplevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_STOPS);
        }

        // ------------------------------------------
        // FreezeLevel Limitation (Freezing Distance)
        // ------------------------------------------
        // Open Price: 	Bid-OpenPrice > FreezeLevel
        if (!(bid - price > freezeLevel)) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }

        final long id = SEQ.incrementAndGet();
        final Order order = new Order(this, id, OrderType.SELL_STOP, instrument, price, volume);
        order.setState(OrderState.PLACED);
        orders.add(order);
        return order;
    }

    @Override
    public void modify(Order order, double price, double volume, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Order order) throws TradeException {
        switch (order.getState()) {
            case REJECTED:
            case CANCELED:
            case EXPIRED:
            case REQUEST_ADD:
            case REQUEST_MODIFY:
            case REQUEST_CANCEL:
                return;
            case STARTED:
                break;
            case PARTIAL:
            case FILLED:
                throw new TradeException(TradeExceptionReason.REJECT);
            case PLACED:
                final Instrument instrument = order.getInstrument();
                double freezeLevel = instrument.pointsToPrice(instrument.freezeLevel());
                final double price = order.getPrice();

                switch (order.getType()) {
                    case BUY:
                    case SELL:
                        // A ordem está aguardando a execução
                        throw new TradeException(TradeExceptionReason.FROZEN);
                    case BUY_LIMIT:
                    case BUY_STOP:
                        double ask = ask(instrument);
                        double askFreezeLevelTop = ask + freezeLevel;
                        double askFreezeLevelBot = ask - freezeLevel;
                        // Pending orders BuyLimit and BuyStop cannot be modified, if the requested order open price 
                        // ranges within the freeze distance from the market price Ask.
                        if (price <= askFreezeLevelTop && price > askFreezeLevelBot) {
                            throw new TradeException(TradeExceptionReason.FROZEN);
                        }
                    case SELL_LIMIT:
                    case SELL_STOP:
                        double bid = bid(instrument);
                        double bidFreezeLevelTop = bid + freezeLevel;
                        double bidFreezeLevelBot = bid - freezeLevel;
                        // Pending orders SellLimit and SellStop cannot be modified, if the requested order open price 
                        // ranges within the freeze distance from the market price Bid.
                        if (price <= bidFreezeLevelTop && price > bidFreezeLevelBot) {
                            throw new TradeException(TradeExceptionReason.FROZEN);
                        }
                }

                break;
        }
        // Após as validações, pode cancelar a ordem
        order.setState(OrderState.CANCELED);
    }

    @Override
    public void modify(Position position, double sl, double tp) throws TradeException {

        checkPositionIsOpen(position);

        // Verifica se os valores de stop estão na zona de congelamento
        checkPositionStopFrozen(position, sl, tp, TradeExceptionReason.INVALID_STOPS);
        checkPositionStopFrozen(position, position.getStopLoss(), position.getTakeProfit(), TradeExceptionReason.FROZEN);

        // Altera os valores
        position.setStopLoss(sl);
        position.setTakeProfit(tp);
    }

    @Override
    public void close(Position position, double price, long deviation) throws TradeException {

        checkPositionIsOpen(position);
        checkPrice(price);

        // Verifica se os valores de stop estão na zona de congelamento
        // Order cannot be closed, if the execution price of its StopLoss or TakeProfit is within the range of freeze distance from the market price.
        checkPositionStopFrozen(position, position.getStopLoss(), position.getTakeProfit(), TradeExceptionReason.FROZEN);

        final Instrument instrument = position.getInstrument();
        double volume = position.volume();
        long id = SEQ.incrementAndGet();
        final OHLC tick = rates(instrument);
        final Instant instant = Instant.ofEpochSecond(tick.time + 5);

        if (position.getType().equals(PositionType.BUY)) {

            // Deve sair com uma operação do tipo SELL
            double bid = bid(instrument);

            checkSellPriceDeviation(bid, price, deviation);

            // Fecha a posição
            final Order order = new Order(this, id, OrderType.SELL, instrument, bid, volume);
            final Deal deal = new Deal(id, id, instant, DealType.SELL, DealEntry.OUT, bid, volume, 0.0, 0.0, 0.0);
            order.setState(OrderState.FILLED);
            order.addDeal(deal);
            position.addOrder(order);
            position = null;

        } else {
            // Deve sair com uma operação do tipo BUY
            double ask = ask(instrument);

            checkBuyPriceDeviation(ask, price, deviation);

            // Fecha a posição
            final Order order = new Order(this, id, OrderType.BUY, instrument, ask, volume);
            final Deal deal = new Deal(id, id, instant, DealType.BUY, DealEntry.OUT, ask, volume, 0.0, 0.0, 0.0);
            order.setState(OrderState.FILLED);
            order.addDeal(deal);
            position.addOrder(order);
            position = null;
        }
    }

    @Override
    public void closePartial(Position position, double price, double volume, long deviation) throws TradeException {

        checkPositionIsOpen(position);
        checkPriceVolume(price, volume);

        if (volume > position.volume()) {
            throw new TradeException(TradeExceptionReason.INVALID_VOLUME);
        }

        // Verifica se os valores de stop estão na zona de congelamento
        checkPositionStopFrozen(position, position.getStopLoss(), position.getTakeProfit(), TradeExceptionReason.FROZEN);

        final Instrument instrument = position.getInstrument();
        long id = SEQ.incrementAndGet();
        final OHLC tick = rates(instrument);
        final Instant instant = Instant.ofEpochSecond(tick.time + 5);

        if (position.getType().equals(PositionType.BUY)) {

            // Deve sair com uma operação do tipo SELL
            double bid = bid(instrument);

            checkSellPriceDeviation(bid, price, deviation);

            // Fechamento parcial da posição
            final Order order = new Order(this, id, OrderType.SELL, instrument, bid, volume);
            final Deal deal = new Deal(id, id, instant, DealType.SELL, DealEntry.OUT, bid, volume, 0.0, 0.0, 0.0);
            order.setState(OrderState.FILLED);
            order.addDeal(deal);
            position.addOrder(order);
        } else {
            // Deve sair com uma operação do tipo BUY
            double ask = ask(instrument);

            checkBuyPriceDeviation(ask, price, deviation);

            // Fecha a posição
            final Order order = new Order(this, id, OrderType.BUY, instrument, ask, volume);
            final Deal deal = new Deal(id, id, instant, DealType.BUY, DealEntry.OUT, ask, volume, 0.0, 0.0, 0.0);
            order.setState(OrderState.FILLED);
            order.addDeal(deal);
            position.addOrder(order);
        }
    }

    private void checkPositionIsOpen(Position position1) throws TradeException {
        if (this.position != position1) {
            throw new TradeException(TradeExceptionReason.POSITION_CLOSED);
        }
    }

    private void checkPriceVolume(double price, double volume) throws TradeException {
        checkPrice(price);

        if (volume <= 0) {
            throw new TradeException(TradeExceptionReason.INVALID_VOLUME);
        }
    }

    private void checkPrice(double price) throws TradeException {
        if (price <= 0) {
            throw new TradeException(TradeExceptionReason.INVALID_PRICE);
        }
    }

    /**
     * Não executar a ordem quando o preço atual muito acima do máximo especificado na ordem
     *
     * @param ask
     * @param price
     * @param deviation
     * @return
     */
    private void checkBuyPriceDeviation(double ask, double price, double deviation) throws TradeException {
        if (deviation > 0 && ask > price + deviation) {
            throw new TradeException(TradeExceptionReason.PRICE_CHANGED);
        }
    }

    /**
     * Não executar a ordem quando preço atual muito abaixo do máximo especificado na ordem
     *
     * @param bid
     * @param price
     * @param deviation
     * @return
     */
    private void checkSellPriceDeviation(double bid, double price, double deviation) throws TradeException {
        if (deviation > 0 && bid < price - deviation) {
            throw new TradeException(TradeExceptionReason.PRICE_CHANGED);
        }
    }

    /**
     * Verifica se os valores de stopLoss e TakeProfit informado estão na zona de congelameto
     *
     * @param position1
     * @param sl
     * @param tp
     * @throws TradeException
     */
    private void checkPositionStopFrozen(Position position1, double sl, double tp, TradeExceptionReason reason) throws TradeException {
        final Instrument instrument = position1.getInstrument();
        // Verifica se os valores de stop estão na freezeLevel
        double freezeLevel = instrument.pointsToPrice(instrument.freezeLevel());
        if (position1.getType().equals(PositionType.BUY)) {
            double bid = bid(instrument);
            if (sl > bid - freezeLevel || tp < bid + freezeLevel) {
                // StopLoss|TakeProfit está na freezeLevel, não é permitido sair da operação 
                throw new TradeException(reason);
            }
        } else {
            double ask = ask(instrument);
            if (sl < ask + freezeLevel || tp > ask - freezeLevel) {
                // StopLoss|TakeProfit está na freezeLevel, não é permitido sair da operação 
                throw new TradeException(reason);
            }
        }
    }

}
