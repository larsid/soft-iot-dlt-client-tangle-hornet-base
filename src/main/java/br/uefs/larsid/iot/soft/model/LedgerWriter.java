package br.uefs.larsid.iot.soft.model;

import br.uefs.larsid.iot.soft.model.transactions.Transaction;
import br.uefs.larsid.iot.soft.services.ILedgerWriter;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * @author Allan Capistrano
 * @version 1.0.0
 */
public class LedgerWriter implements ILedgerWriter, Runnable {

  private boolean debugModeValue;

  private String urlApi;
  private Thread DLTOutboundMonitor;
  private final BlockingQueue<Transaction> DLTOutboundBuffer;
  private static final Logger logger = Logger.getLogger(
    LedgerWriter.class.getName()
  );

  private static final String ENDPOINT = "message";

  public LedgerWriter(String protocol, String url, int port, int bufferSize) {
    this.urlApi = String.format("%s://%s:%s", protocol, url, port);
    this.DLTOutboundBuffer = new ArrayBlockingQueue<Transaction>(bufferSize);
  }

  public void start() {
    if (this.DLTOutboundMonitor == null) {
      this.DLTOutboundMonitor = new Thread(this);
      this.DLTOutboundMonitor.setName(
          "CLIENT_TANGLE_HORNET/DLT_OUTBOUND_MONITOR"
        );
      this.DLTOutboundMonitor.start();
    }
  }

  public void stop() {
    this.DLTOutboundMonitor.interrupt();
  }

  @Override
  public void run() {
    Gson gson = new Gson();

    while (!this.DLTOutboundMonitor.isInterrupted()) {
      try {
        Transaction transaction = this.DLTOutboundBuffer.take();
        transaction.setPublishedAt(System.currentTimeMillis());

        String transactionJson = gson.toJson(transaction);

        this.createMessage(transaction.getType().name(), transactionJson);
      } catch (InterruptedException ex) {
        this.DLTOutboundMonitor.interrupt();
      }
    }
  }

  @Override
  public void put(Transaction transaction) throws InterruptedException {
    this.DLTOutboundBuffer.put(transaction);
  }

  public void createMessage(String index, String content) {
    try {
      URL url = new URL(String.format("%s/%s", this.urlApi, ENDPOINT));

      // Abrir conexão HTTP
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true); // Permitir a escrita no corpo da requisição

      String requestBody = String.format(
        "{\"index\": \"%s\",\"content\": %s}",
        index,
        content
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
          logger.info("API response: " + response.toString());
        }
      } else {
        if (debugModeValue) {
          logger.info("Error in HTTP request: " + responseCode);
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
