package com.github.nidorx.jtrade.broker.impl.metatrader;

import com.github.nidorx.jtrade.broker.impl.metatrader.model.Topic;
import com.github.nidorx.jtrade.core.Tick;
import com.github.nidorx.jtrade.core.Account;
import com.github.nidorx.jtrade.broker.Broker;
import com.github.nidorx.jtrade.core.Instrument;
import com.github.nidorx.jtrade.core.Rate;
import com.github.nidorx.jtrade.core.exception.TradeException;
import com.github.nidorx.jtrade.core.exception.TradeExceptionReason;
import com.github.nidorx.jtrade.broker.impl.metatrader.model.Command;
import com.github.nidorx.jtrade.core.trading.Order;
import com.github.nidorx.jtrade.core.trading.OrderFilling;
import com.github.nidorx.jtrade.core.trading.OrderState;
import com.github.nidorx.jtrade.core.trading.OrderType;
import com.github.nidorx.jtrade.core.trading.Position;
import com.github.nidorx.jtrade.core.Strategy;
import com.github.nidorx.jtrade.core.TimeFrame;
import com.github.nidorx.jtrade.core.impl.InstrumentImpl;
import com.github.nidorx.jtrade.util.StringDelimitedParser;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Integração com o Metatrader usando socket
 *
 * https://www.mql5.com/en/articles/1284
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class MetatraderBroker extends Broker {

    private static final Logger LOGGER = Logger.getLogger(MetatraderBroker.class.getName());

    private final Map<String, MT5SocketClient> CLIENTS = new ConcurrentHashMap<>();

    private final Map<Instrument, MT5SocketClient> CLIENTS_BY_INSTRUMENT = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException, Exception {
        MetatraderBroker broker = new MetatraderBroker();
        broker.connect("127.0.0.1", 23456);

        broker.register(new Strategy() {
            @Override
            public String getName() {
                return "TesteMT5";
            }

            @Override
            public void initialize(Account account) {

            }

            @Override
            public void onTick(Tick tick) {
//                System.out.println(tick);
            }

            @Override
            public void onRate(Rate rate) {
                if (rate.timeframe.equals(TimeFrame.M30)) {
                    // @TODO: Testar buy e verificar posições abertas
                    System.out.println(rate);

                    if (this.getOrders().isEmpty() || this.getPositions().isEmpty()) {
                        try {
                            this.buy(0.2);
                        } catch (TradeException ex) {
                            Logger.getLogger(MetatraderBroker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                // 1514859960
            }

            @Override
            protected void onRelease() {

            }
        }, "EURUSD");
    }

    /**
     * Permite criar uma conexão com o EA
     *
     * Somente é permitido criar conexões com o EA que estejam operando a mesma
     * conta
     *
     * @param host
     * @param port
     * @throws java.io.IOException
     */
    public void connect(String host, int port) throws IOException {
        if (CLIENTS.containsKey(host + ":" + port)) {
            return;
        }

        final MT5SocketClient client = new MT5SocketClient(host, port);
        client.onConnect(() -> {

            // Obtém informação sobre o instrumento
            while (true) {
                try {
                    // "SYMBOL BASE QUOTE DIGITS CONTRACT_SIZE TICK_VALUE TIME BID ASK STOPS_LEVEL FREEZE_LEVEL"
                    String response = client.exec(Command.SYMBOL);
                    StringDelimitedParser p = new StringDelimitedParser(response, ' ');

                    String symbol = p.pop();
                    String base = p.pop();
                    String quote = p.pop();
                    int digits = p.popInt();
                    double contractSize = p.popDouble();
                    double tickValue = p.popDouble();
                    long time = p.popLong();
                    double bid = p.popDouble();
                    double ask = p.popDouble();
                    int stopLevel = p.popInt();
                    int freezeLevel = p.popInt();

                    createInstrument(symbol, base, quote, digits, contractSize, tickValue, bid, ask);

                    // @TODO: Permitir atualizar o stop e freeze levels da conta, criar novo tópico
                    InstrumentImpl instrument = (InstrumentImpl) getInstrument(symbol);
                    instrument.setStopLevel(stopLevel);
                    instrument.setFreezeLevel(freezeLevel);

                    CLIENTS_BY_INSTRUMENT.put(instrument, client);

                    setServerTime(Instant.ofEpochSecond(time));
                    break;
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }

            // Observa informações sobre a conta
            client.subscribe(Topic.ACCOUNT, (account) -> {
                this.setAccount((Account) account);
            });

            // Observa novos ticks
            client.subscribe(Topic.TICK, (tick) -> {
                this.processTick((Tick) tick);
            });

            // Observa novos candles
            client.subscribe(Topic.RATES, (rate) -> {
                try {
                    this.processRate((Rate) rate);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });

            // Sempre que um novo Server for adicionado, faz a conexão com novo server
            client.subscribe(Topic.SERVERS, (servers) -> {
                for (Integer serverPort : (List<Integer>) servers) {
                    if (serverPort.equals(port)) {
                        continue;
                    }
                    try {
                        connect(host, serverPort);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            });
        });

        client.onDisconnect(() -> {
            System.out.println("Desconectado");
        });

        // Finalmente, conecta-se com o server
        client.connect();

        // Salva referencia para o cliente
        CLIENTS.put(host + ":" + port, client);
    }

    @Override
    public String getName() {
        return "Metatrader";
    }

    @Override
    public void buy(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        try {
            final MT5SocketClient client = CLIENTS_BY_INSTRUMENT.get(instrument);
            if (client == null) {
                return;
            }

            client.exec(Command.BUY, price, volume, deviation, sl, tp);

            // Executado com sucesso, adicionar uma ordem SEM NÚMERO com stado OrderState.REQUEST_ADD na lista de ordens abertas
            List<Order> orders = getOrders(instrument);

            // Long id, Long position, Instant time, OrderType type, OrderState state, OrderFilling filling,
            // double price, double volume, double stopLoss, double takeProfit, double stopLimit
            orders.add(new Order(0L, 0L, Instant.now(), OrderType.BUY, OrderState.REQUEST_ADD, OrderFilling.FOK, price, volume, sl, tp, 0));

            setOrders(instrument, orders);

        } catch (IOException | InterruptedException ex) {
            throw new TradeException(TradeExceptionReason.ERROR, ex);
        } catch (MT5Exception ex) {
            // @TODO: Tratar erros
            throw new TradeException(TradeExceptionReason.ERROR, ex);
        }
    }

    @Override
    public void sell(Instrument instrument, double price, double volume, long deviation, double sl, double tp) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Order order) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close(Position position, double price, long deviation) throws TradeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
