package dlt.client.tangle.hornet.model;

import dlt.client.tangle.hornet.model.tangle.Message;
import dlt.client.tangle.hornet.model.tangle.Payload;
import dlt.client.tangle.hornet.model.transactions.Transaction;
import dlt.client.tangle.hornet.services.ILedgerReader;
import dlt.client.tangle.hornet.services.ILedgerSubscriber;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Allan Capistrano, Antonio Crispim, Uellington Damasceno
 * @version 1.0.0
 */
public class LedgerReader implements ILedgerReader, Runnable {

  private boolean debugModeValue;

  private String urlApi;
  private Thread DLTInboundMonitor;
  private final Map<String, Set<ILedgerSubscriber>> topics;
  private ZMQServer server;

  private static final Logger logger = Logger.getLogger(
    LedgerReader.class.getName()
  );

  private static final String ENDPOINT = "/api/v1/messages";
  private static final String ENDPOINT_MESSAGE_ID = "/api/v1/messages";

  public LedgerReader(String protocol, String url, int port) {
    this.urlApi = String.format("%s://%s:%s", protocol, url, port);
    this.topics = new HashMap<>();
  }

  public void start() {
    if (this.DLTInboundMonitor == null) {
      this.DLTInboundMonitor = new Thread(this);
      this.DLTInboundMonitor.setName("CLIENT_TANGLE/DLT_INBOUND_MONITOR");
      this.server.start();
      this.DLTInboundMonitor.start();
    }
    logger.log(Level.INFO, "LEDGER READER - Complete URL: {0}", this.urlApi);
  }

  public void stop() {
    logger.info("LEDGER READER IS STOPPING.");
    this.server.stop();
    this.DLTInboundMonitor.interrupt();
  }

  /**
   * Get all transactions by a given index.
   *
   * @param index String - Message index
   */
  @Override
  public List<Transaction> getTransactionsByIndex(String index) {
    String response = null;
    List<Transaction> transactions = new ArrayList<>();

    try {
      URL url = new URL(
        String.format("%s/%s/%s", this.urlApi, ENDPOINT, index)
      );
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException(
          "HTTP error code : " + conn.getResponseCode()
        );
      }

      BufferedReader br = new BufferedReader(
        new InputStreamReader((conn.getInputStream()))
      );

      String temp = null;

      while ((temp = br.readLine()) != null) {
        response = temp;
      }

      conn.disconnect();

      transactions =
        Transaction.jsonArrayInStringToTransaction(response, debugModeValue);

      return transactions;
    } catch (MalformedURLException mue) {
      if (debugModeValue) {
        logger.severe(mue.getMessage());
      }
    } catch (IOException ioe) {
      if (debugModeValue) {
        logger.severe(ioe.getMessage());
      }
    }

    return transactions;
  }

  /**
   * Get a transaction by a given transaction ID.
   *
   * @param transactionId String - transaction ID.
   */
  @Override
  public Transaction getTransactionById(String transactionId) {
    String response = null;

    try {
      URL url = new URL(
        String.format(
          "%s/%s/%s/",
          this.urlApi,
          ENDPOINT_MESSAGE_ID,
          transactionId
        )
      );
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException(
          "HTTP error code : " + conn.getResponseCode()
        );
      }

      BufferedReader br = new BufferedReader(
        new InputStreamReader((conn.getInputStream()))
      );

      String temp = null;

      while ((temp = br.readLine()) != null) {
        response = temp;
      }

      conn.disconnect();

      return Transaction.getTransactionObjectByType(
        Payload.stringToPayload(response).getData(),
        debugModeValue
      );
    } catch (MalformedURLException mue) {
      if (debugModeValue) {
        logger.severe(mue.getMessage());
      }
    } catch (IOException ioe) {
      if (debugModeValue) {
        logger.severe(ioe.getMessage());
      }
    }

    return null;
  }

  @Override
  public void subscribe(String topic, ILedgerSubscriber subscriber) {
    if (topic != null) {
      Set<ILedgerSubscriber> subscribers = this.topics.get(topic);
      if (subscribers != null) {
        subscribers.add(subscriber);
      } else {
        subscribers = new HashSet<ILedgerSubscriber>();
        subscribers.add(subscriber);
        this.topics.put(topic, subscribers);
        this.server.subscribe(topic);
      }
    }
  }

  @Override
  public void unsubscribe(String topic, ILedgerSubscriber subscriber) {
    if (topic != null) {
      Set<ILedgerSubscriber> subscribers = this.topics.get(topic);
      if (subscribers != null && !subscribers.isEmpty()) {
        subscribers.remove(subscriber);
        if (subscribers.isEmpty()) {
          this.server.unsubscribe(topic);
          this.topics.remove(topic);
        }
      }
    }
  }

  @Override
  public void run() {
    while (!this.DLTInboundMonitor.isInterrupted()) {
      try {
        String receivedMessage = this.server.take();

        if (receivedMessage != null && receivedMessage.contains("/")) {
          Gson gson = new Gson();
          String[] data = receivedMessage.split("/", 2);
          String topic = data[0];

          Message message = gson.fromJson(data[1], Message.class);

          notifyAll(
            topic,
            Transaction.getTransactionObjectByType(
              message.getPayload().getData(),
              debugModeValue
            ),
            message.getId()
          );
        }
      } catch (InterruptedException ex) {
        logger.info(ex.getMessage());
        this.DLTInboundMonitor.interrupt();
      }
    }
  }

  private void notifyAll(String topic, Object object, Object object2) {
      if (topic == null || topic.isEmpty()) {
          return;
      }
      Set<ILedgerSubscriber> subscribers = this.getSubscribers(topic);
      if (subscribers == null || subscribers.isEmpty()) {
          return;
      }
      subscribers.forEach(sub -> sub.update(object, object2));
  }

  private Set<ILedgerSubscriber> getSubscribers(String topic) {
      Set<ILedgerSubscriber> subscribers = new HashSet<>();

      if (topics.containsKey(topic)) {
          subscribers.addAll(topics.get(topic));
      }

      for (Map.Entry<String, Set<ILedgerSubscriber>> entry : topics.entrySet()) {
          String registeredTopic = entry.getKey();
          if (registeredTopic.matches(topic.replace("*", ".*"))) {
              subscribers.addAll(entry.getValue());
          }
      }
      return subscribers;
  }

  public String getUrlApi() {
    return urlApi;
  }

  public void setUrlApi(String urlApi) {
    this.urlApi = urlApi;
  }

  public void setServer(ZMQServer server) {
    this.server = server;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}
