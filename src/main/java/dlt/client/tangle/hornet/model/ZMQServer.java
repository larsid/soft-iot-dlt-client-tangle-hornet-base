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
    private static final Logger logger = Logger.getLogger(
            ZMQServer.class.getName()
    );

    public ZMQServer(
            int bufferSize,
            String socketProtocol,
            String socketURL,
            String socketPort
    ) {
        this.DLTInboundBuffer = new ArrayBlockingQueue<String>(bufferSize);
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
        this.serverThread.interrupt();
        this.serverListener.close();
    }

    public void subscribe(String topic) {
        logger.log(Level.INFO, "Subscribe: {0}", topic);
        this.serverListener.subscribe(topic);
    }

    public void unsubscribe(String topic) {
        this.serverListener.unsubscribe(topic);
    }

    public String take() throws InterruptedException {
        return this.DLTInboundBuffer.take();
    }

    @Override
    public void run() {
        this.connectWithRetry(100, 5*1000);
        while (!this.serverThread.isInterrupted()) {
            byte[] reply = serverListener.recv(0);
            String[] data = (new String(reply).split(" "));

            this.putReceivedMessageBuffer(String.format("%s/%s", data[0], data[1]));
        }
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
        boolean connected = false;

        while (attempts < maxRetries && !connected) {
            try {
                this.serverListener.connect(socketURL);
                logger.log(Level.INFO, "Conectado ao servidor ZMQ: {0}", socketURL);
                connected = true;
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

        if (!connected) {
            throw new RuntimeException("Não foi possível conectar ao servidor ZMQ após " + maxRetries + " tentativas.");
        }
    }

}
