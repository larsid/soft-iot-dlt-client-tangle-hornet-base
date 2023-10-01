package br.uefs.larsid.iot.soft.model;

import br.uefs.larsid.iot.soft.enums.TransactionType;
import br.uefs.larsid.iot.soft.model.transactions.Evaluation;
import br.uefs.larsid.iot.soft.model.transactions.Status;
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
public class LedgerWriter implements ILedgerWriter {

  private boolean debugModeValue;

  private String urlApi;
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
    // TODO: Temporário, alterar para inicialização da thread
    Gson gson = new Gson();
    Transaction transaction1 = new Evaluation(
      "source",
      "target",
      TransactionType.REP_EVALUATION,
      1
    );
    Transaction transaction2 = new Status(
      "source4",
      "group4",
      false,
      3,
      4,
      true
    );

    Transaction transactionByQueue;
    try {
      this.put(transaction1);
      this.put(transaction2);

      while (this.DLTOutboundBuffer.size() > 0) {
        transactionByQueue = this.DLTOutboundBuffer.take();
        transactionByQueue.setPublishedAt(System.currentTimeMillis());

        String transactionByQueueJson = gson.toJson(transactionByQueue);

        this.createMessage(
            transactionByQueue.getType().name(),
            transactionByQueueJson
          );
      }
    } catch (InterruptedException ie) {
      if (debugModeValue) {
        logger.severe(ie.getMessage());
      }
    }
  }

  public void stop() {
    // TODO: Temporário, remover depois
    logger.info("Finishing the soft-iot-dlt-client-tangle-hornet");
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
