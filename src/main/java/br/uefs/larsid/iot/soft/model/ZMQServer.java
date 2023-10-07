package br.uefs.larsid.iot.soft.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    this.socketURL =
      String.format("%s://%s:%s", socketProtocol, socketURL, socketPort);
  }

  public void start() {
    if (this.serverThread == null) {
      logger.info("Socket URL: " + this.socketURL);

      this.serverListener.connect(this.socketURL);
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
    logger.info("Subscribe: " + topic);
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
}
