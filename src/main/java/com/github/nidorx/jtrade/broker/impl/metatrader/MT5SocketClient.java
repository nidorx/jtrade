package com.github.nidorx.jtrade.broker.impl.metatrader;

import com.github.nidorx.jtrade.util.Cancelable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Encapsula o mecanismo de resposta asíncrona do server
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
class ResponseAsync {

    private String result;

    private final CountDownLatch latch = new CountDownLatch(1);

    public void resolve(String result) {
        this.result = result;
        latch.countDown();
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public String getResult() {
        return result;
    }

}

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class MT5SocketClient extends Observable {

    private final String host;
    private final int port;
    private Socket socket;

    private OutputStream outputStream;

    /**
     * New line
     */
    private static final String CRLF = "\r\n";

    /**
     * Permite sequenciar as requisições
     */
    private static final AtomicInteger REQUEST_SEQUENCE = new AtomicInteger(0x1);

    /**
     * Abriga as requisições sincronas executadas a partir do Java para o MT5 (via socket)
     *
     * O método de requisição sempre aguarda a resposta do MT5 para continuar o processamento
     */
    private static final Map<Integer, ResponseAsync> REQUESTS = new ConcurrentHashMap<>();

    private static final Map<Topic, List<Consumer<String>>> SUBSCRIPTIONS = new ConcurrentHashMap<>();

    public MT5SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        outputStream = socket.getOutputStream();
        final Thread receivingThread = new Thread() {
            @Override
            public void run() {
                try {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onMessage(line);
                    }
                } catch (IOException ex) {
                    notifyObservers(ex);
                }
            }
        };
        receivingThread.start();
    }

    /**
     * Faz o tratamento da mensagem proveniente do EA
     *
     * @param message
     */
    private void onMessage(String message) {
        System.out.println(message);
        if (message.startsWith("#")) {
            // Está respondendo a uma mensagem sincronizada, no formato "#<ID_REQUISICAO>#<CONTEUDO>"

            final String[] parts = message.split("#", 3);
            if (parts.length != 3) {
                // Formato de mensagem inválida
                return;
            }

            final Integer id = Integer.valueOf(parts[1]);
            if (REQUESTS.containsKey(id)) {
                final String content = parts[2];
                REQUESTS.get(id).resolve(content);
                REQUESTS.remove(id);
            }
        } else if (message.startsWith("\\*")) {
            // Está respondendo a um topico, no formato "*<ID_TOPICO>*<CONTEUDO>"

            final String[] parts = message.split("\\*", 3);
            if (parts.length != 3) {
                // Formato de mensagem inválida
                return;
            }

            final Integer topicId = Integer.valueOf(parts[1]);

            Topic topic = Topic.getByCode(topicId);
            if (topic == null) {
                return;
            }
            final String content = parts[2];

            if (!SUBSCRIPTIONS.containsKey(topic)) {
                return;
            }

            //  Informar aos interessados sobre o topico
            SUBSCRIPTIONS.get(topic).forEach((callback) -> {
                callback.accept(content);
            });
        } else {
            notifyObservers(message);
        }
    }

    @Override
    public void notifyObservers(Object arg) {
        super.setChanged();
        super.notifyObservers(arg);
    }

    /**
     * Envia uma mensagem para o EA.
     *
     * @param message
     * @throws java.io.IOException
     */
    public void send(String message) throws IOException {
        outputStream.write((message + CRLF).getBytes());
        outputStream.flush();
    }

    /**
     * Permite susbrever em um tópico específico
     *
     * @param topic
     * @param callback
     * @return
     * @throws IOException
     */
    public Cancelable subscribe(final Topic topic, final Consumer<String> callback) throws IOException {

        this.send("TOPIC_" + topic.code + "_1");

        if (!SUBSCRIPTIONS.containsKey(topic)) {
            SUBSCRIPTIONS.put(topic, new CopyOnWriteArrayList<>());
        }

        if (!SUBSCRIPTIONS.get(topic).contains(callback)) {
            SUBSCRIPTIONS.get(topic).add(callback);
        }

        return () -> {
            if (!SUBSCRIPTIONS.containsKey(topic)) {
                return;
            }

            if (!SUBSCRIPTIONS.get(topic).contains(callback)) {
                return;
            }

            SUBSCRIPTIONS.get(topic).remove(callback);
        };
    }

    /**
     * Executa um comando no EA (JTrade EA)
     *
     * A thread atual fica em espera ate que o EA responda
     *
     * @param command
     * @param args
     * @return
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     * @throws com.github.nidorx.jtrade.broker.impl.metatrader.MT5Exception
     */
    public String exec(Command command, Object... args) throws IOException, InterruptedException, MT5Exception {
        final Integer id = REQUEST_SEQUENCE.getAndIncrement();
        final ResponseAsync response = new ResponseAsync();

        REQUESTS.put(id, response);

        // No formato: "<NUM_REQUISICAO>_<COD_COMANDO>_<PARAM_1>_<PARAM_2>_<PARAM_N>"
        final StringBuilder message = new StringBuilder();

        message
                .append(id)
                .append('_')
                .append(command.code);

        if (args != null && args.length > 0) {
            for (Object arg : args) {
                message.append('_').append(arg);
            }
        }

        try {
            send(message.toString());

            // Aguarda a resposta do EA para continuar a execução
            response.await();
        } catch (IOException | InterruptedException ex) {

            // Limpa a referencia
            REQUESTS.remove(id);

            throw ex;
        }

        // A resposta, quando erro, retorna apenas "@<NUMERO_DO_ERRO>"
        String result = response.getResult();
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        if (error != 0) {
            StringBuilder signature = new StringBuilder(command.name + "(");
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    signature.append(" ").append(arg);
                }
            }
            signature.append(')');
            throw new MT5Exception(error, signature.toString());
        }

        result = ix >= 0 ? result.substring(0, ix) : result;
        return result;
    }

    /**
     * Close the socket
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            notifyObservers(ex);
        }
    }
}
