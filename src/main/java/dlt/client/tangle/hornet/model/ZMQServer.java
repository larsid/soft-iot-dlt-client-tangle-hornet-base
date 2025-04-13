package dlt.client.tangle.hornet.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

/**
 *
 * @author Allan Capistrano, Antonio Crispim, Uellington Damasceno
 * @version 1.0.0
 */
public class ZMQServer implements Runnable {

    private Thread serverThread;
    private final BlockingQueue<String> DLTInboundBuffer;
    private ZMQ.Socket serverListener;
    private String socketURL;

    private volatile boolean isConnected = false;

    private static final Logger logger = Logger.getLogger(
            ZMQServer.class.getName()
    );

    public ZMQServer(
            int bufferSize,
            String socketProtocol,
            String socketURL,
            String socketPort
    ) {
        this.DLTInboundBuffer = new ArrayBlockingQueue<>(bufferSize);
        this.serverListener = ZMQ.context(1).socket(SocketType.SUB);
        this.socketURL
                = String.format("%s://%s:%s", socketProtocol, socketURL, socketPort);
    }

    public void start() {
        if (this.serverThread == null) {
            logger.log(Level.INFO, "Socket URL: {0}", this.socketURL);

            this.serverThread = new Thread(this);
            this.serverThread.setName("CLIENT_TANGLE/ZMQ_SERVER");
            this.serverThread.start();
        }
    }

    public void stop() {
        logger.info("CLIENT_TANGLE/ZMQ_SERVER - ZMQ SERVER IS STOPPING.");
        this.serverThread.interrupt();
        this.serverListener.close();
    }

    public void subscribe(String topic) {
        if (!isConnected) {
            logger.log(Level.WARNING, "Tentativa de subscribe antes da conexão: {0}", topic);
            return;
        }
        logger.log(Level.INFO, "Subscribe: {0}", topic);
        this.serverListener.subscribe(topic);
    }

    public void unsubscribe(String topic) {
        if (!isConnected) {
            logger.log(Level.WARNING, "Tentativa de unsubscribe antes da conexão: {0}", topic);
            return;
        }
        this.serverListener.unsubscribe(topic);
    }

    public String take() throws InterruptedException {
        return this.DLTInboundBuffer.take();
    }

    @Override
    public void run() {
        String response;
        logger.info("Estabelecendo conexão com o servidor ZMQ");
        logger.log(Level.INFO, "Instância de ZmqServer: {0}", System.identityHashCode(this));

        this.connectWithRetry(100, 5 * 1000);
        logger.info("CLIENT_TANGLE/ZMQ_SERVER - WAITING MESSAGE");
        while (!this.serverThread.isInterrupted()) {
            byte[] reply = serverListener.recv(0);
            response = new String(reply);
            String[] data = response.split(" ");

            if (data.length == 2) {
                this.putReceivedMessageBuffer(String.format("%s/%s", data[0], data[1]));
            } else {
                logger.log(Level.WARNING, "Mensagem malformada recebida do ZMQ (esperado 2 tokens, obtido {1}): {0}",
                        new Object[]{response, data.length});
            }
        }
        logger.info("CLIENT_TANGLE/ZMQ_SERVER - STOPED");
    }

    private void putReceivedMessageBuffer(String receivedMessage) {
        try {
            this.DLTInboundBuffer.put(receivedMessage);
        } catch (InterruptedException ex) {
            logger.severe(ex.getMessage());
        }
    }

    public void connectWithRetry(int maxRetries, int retryIntervalMs) {
        int attempts = 0;

        if (this.isConnected) {
            logger.warning("Conexão já está ativa. Ignorando tentativa.");
            return;
        }

        this.isConnected = false;

        while (attempts < maxRetries && !isConnected) {
            try {
                logger.log(Level.INFO, "Tentando conectar ao Servidor ZMQ: {0}", socketURL);
                this.serverListener.connect(socketURL);
                this.serverListener.subscribe("");
                logger.log(Level.INFO, "Conectado ao servidor ZMQ: {0}", socketURL);
                this.isConnected = true;
            } catch (Exception e) {
                attempts++;
                logger.log(Level.SEVERE, "Falha ao conectar (tentativa {0}): {1}", new Object[]{attempts, e.getMessage()});
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(retryIntervalMs); // Aguarda antes de tentar novamente
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Preserva o estado de interrupção
                    }
                } else {
                    logger.log(Level.SEVERE, "Falha ap\u00f3s {0} tentativas. Conex\u00e3o recusada.", maxRetries);
                }
            }
        }

        if (!this.isConnected) {
            throw new RuntimeException("Não foi possível conectar ao servidor ZMQ após " + maxRetries + " tentativas.");
        }
    }

}
