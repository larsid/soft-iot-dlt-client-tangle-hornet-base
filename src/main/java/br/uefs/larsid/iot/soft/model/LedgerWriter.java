package br.uefs.larsid.iot.soft.model;

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
import java.util.logging.Logger;

public class LedgerWriter implements ILedgerWriter {

  private String urlApi;
  private static final Logger logger = Logger.getLogger(
    LedgerWriter.class.getName()
  );

  public void start() {
    // TODO: Temporário, remover depois
    Gson gson = new Gson();
    Transaction transaction = new Status("source", "group", true, 2, 3, false);
    this.createMessage("LB_ENTRY", gson.toJson(transaction));
  }

  public void stop() {
    logger.info("Finishing the soft-iot-dlt-client-tangle-hornet");
  }

  @Override
  public void put(Transaction transaction) throws InterruptedException {
    // TODO: Implementar o método juntamente com a thread
    throw new UnsupportedOperationException("Unimplemented method 'put'");
  }

  public void createMessage(String index, String content) {
    String endpoint = "message";
    URL url;

    try {
      // URL do endpoint
      url = new URL(String.format("%s/%s", this.urlApi, endpoint));

      // Abrir conexão HTTP
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Configurar o método HTTP para POST
      connection.setRequestMethod("POST");

      // Configurar cabeçalhos da requisição (opcional)
      connection.setRequestProperty("Content-Type", "application/json");

      // Permitir a escrita no corpo da requisição
      connection.setDoOutput(true);

      logger.info(content);

      // Corpo da requisição
      String requestBody = String.format(
        "{\"index\": \"%s\",\"content\": %s}", //TODO: Permitir na API content como um JSON
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

        // A resposta da API está em 'response.toString()'
        logger.info("Resposta da API: " + response.toString());
      } else {
        // Tratar erros aqui, se necessário
        logger.info("Erro na requisição HTTP: " + responseCode);
      }

      // Fechar a conexão
      connection.disconnect();
    } catch (IOException ioe) {
      logger.severe(ioe.getMessage());
    }
  }

  public String getUrlApi() {
    return urlApi;
  }

  public void setUrlApi(String urlApi) {
    this.urlApi = urlApi;
  }
}
