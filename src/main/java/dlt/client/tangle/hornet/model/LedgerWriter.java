package dlt.client.tangle.hornet.model;

import com.google.gson.Gson;
import dlt.client.tangle.hornet.model.transactions.IndexTransaction;
import dlt.client.tangle.hornet.services.ILedgerWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Allan Capistrano
 * @version 1.1.0
 */
public class LedgerWriter implements ILedgerWriter, Runnable {

  private boolean debugModeValue;

  private String urlApi;
  private Thread DLTOutboundMonitor;
  private final BlockingQueue<IndexTransaction> DLTOutboundBuffer;
  private static final Logger logger = Logger.getLogger(
    LedgerWriter.class.getName()
  );

  private static final String ENDPOINT = "/api/v1/messages";

  public LedgerWriter(String protocol, String url, int port, int bufferSize) {
    this.urlApi = String.format("%s://%s:%s", protocol, url, port);
    this.DLTOutboundBuffer =
      new ArrayBlockingQueue<IndexTransaction>(bufferSize);
  }

  public void start() {
    if (this.DLTOutboundMonitor == null) {
      this.DLTOutboundMonitor = new Thread(this);
      this.DLTOutboundMonitor.setName(
          "CLIENT_TANGLE_HORNET/DLT_OUTBOUND_MONITOR"
        );
      this.DLTOutboundMonitor.start();
    }
    logger.log(Level.INFO, "LEDGER WRITER - Complete URL: {0}", this.urlApi);
  }

  public void stop() {
    this.DLTOutboundMonitor.interrupt();
  }

  @Override
  public void run() {
    Gson gson = new Gson();

    while (!this.DLTOutboundMonitor.isInterrupted()) {
      try {
        IndexTransaction indexTransaction = this.DLTOutboundBuffer.take();

        indexTransaction
          .getTransaction()
          .setPublishedAt(System.currentTimeMillis());

        String transactionJson = gson.toJson(indexTransaction.getTransaction());

        this.createMessage(indexTransaction.getIndex(), transactionJson);
      } catch (InterruptedException ex) {
        this.DLTOutboundMonitor.interrupt();
      }
    }
  }

  /**
   * Put a transaction to be published on Tangle Hornet
   *
   * @param indexTransaction IndexTransaction - Transação que será publicada.
   */
  @Override
  public void put(IndexTransaction indexTransaction)
    throws InterruptedException {
    this.DLTOutboundBuffer.put(indexTransaction);
  }

  /**
   * Create a new message in Tangle Hornet.
   *
   * @param index String - Index of the message.
   * @param data String - Data of the message.
   */
  public void createMessage(String index, String data) {
    try {
      URL url = new URL(String.format("%s/%s", this.urlApi, ENDPOINT));

      // Abrir conexão HTTP
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true); // Permitir a escrita no corpo da requisição

      String requestBody = String.format(
        "{\"index\": \"%s\",\"data\": %s}",
        index,
        data
      );

      // Escrever o corpo da requisição no OutputStream
      try (
        DataOutputStream outputStream = new DataOutputStream(
          connection.getOutputStream()
        )
      ) {
        outputStream.writeBytes(requestBody);
        outputStream.flush();
      }

      // Obter a resposta da requisição
      int responseCode = connection.getResponseCode();

      // Ler a resposta da API
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(
          new InputStreamReader(connection.getInputStream())
        );
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        if (debugModeValue) {
          logger.log(Level.INFO, "API response: {0}", response.toString());
        }
      } else {
        if (debugModeValue) {
          logger.log(Level.INFO, "Error in HTTP request: {0}", responseCode);
        }
      }

      // Fechar a conexão
      connection.disconnect();
    } catch (IOException ioe) {
      if (debugModeValue) {
        logger.severe(ioe.getMessage());
      }
    }
  }

  @Override
  public String getUrl() {
    return urlApi;
  }

  public void setUrlApi(String urlApi) {
    this.urlApi = urlApi;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}
