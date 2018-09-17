package com.github.nidorx.jtrade.broker.impl.metatrader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
public class SocketClient extends Observable {

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

    public SocketClient(String host, int port) {
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
        if (message.startsWith("#")) {
            // Está respondendo a uma mensagem sincronizada, no formato "#ID_REQUISICAO#<CONTEUDO>"

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
     * Envia uma mensagem para o MT5.
     *
     * A thread atual fica em espera ate que o EA responda
     *
     * @param message
     * @return
     * @throws java.lang.InterruptedException
     */
    public String send(String message) throws InterruptedException {
        Integer id = REQUEST_SEQUENCE.getAndIncrement();
        final ResponseAsync response = new ResponseAsync();
        REQUESTS.put(id, response);

        try {
            outputStream.write(("" + id + " " + message + CRLF).getBytes());
            outputStream.flush();
        } catch (IOException ex) {
            notifyObservers(ex);
        }

        // Aguarda a resposta do EA para continuar a execução
        response.await();

        return response.getResult();
    }

    /**
     * Executa um comando no Abstração para execução de comandos no terminal (JTrade EA)
     *
     * @param command
     * @param args
     * @return
     * @throws java.lang.InterruptedException
     * @throws com.github.nidorx.jtrade.broker.impl.metatrader.MT5Exception
     */
    public String exec(Command command, Object... args) throws InterruptedException, MT5Exception {
        StringBuilder commandStr = new StringBuilder(command.code + " ");

        if (args != null && args.length > 0) {
            for (Object arg : args) {
                commandStr.append('_').append(arg);
            }
        }

        String result = send(commandStr.toString());
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
