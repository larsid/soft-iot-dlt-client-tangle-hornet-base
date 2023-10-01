package br.uefs.larsid.iot.soft.model;

import br.uefs.larsid.iot.soft.services.ILedgerReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Allan Capistrano
 * @version 1.0.0
 */
public class LedgerReader implements ILedgerReader {

  private boolean debugModeValue;

  private String urlApi;
  private static final Logger logger = Logger.getLogger(
    LedgerReader.class.getName()
  );

  private static final String ENDPOINT = "message";
  private static final String ENDPOINT_MESSAGE_ID = "message/messageId";

  public LedgerReader(String protocol, String url, int port) {
    this.urlApi = String.format("%s://%s:%s", protocol, url, port);
  }

  public void start() {
    // TODO: Temporário, remover depois
    logger.info(this.getMessagesByIndex("LB_ENTRY_REPLY"));
    logger.info(
      this.getMessageByMessageId(
          "1ce1acc6b9d6fc82713cac49356fd693d9aec070ea20a8b671ace6416477962f"
        )
    );
  }

  public void stop() {
    // TODO: Temporário, remover depois
    logger.info("Finishing the soft-iot-dlt-client-tangle-hornet");
  }

  /**
   * Get all messages by a given index.
   *
   * @param index String - Message index
   */
  @Override
  public String getMessagesByIndex(String index) {
    String response = null;

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

      return response;
    } catch (MalformedURLException mue) {
      if (debugModeValue) {
        logger.severe(mue.getMessage());
      }
    } catch (IOException ioe) {
      if (debugModeValue) {
        logger.severe(ioe.getMessage());
      }
    }

    return response;
  }

  /**
   * Get a message by a given message ID.
   *
   * @param messageId String - Message ID.
   */
  @Override
  public String getMessageByMessageId(String messageId) {
    String response = null;

    try {
      URL url = new URL(
        String.format("%s/%s/%s/", this.urlApi, ENDPOINT_MESSAGE_ID, messageId)
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

      return response;
    } catch (MalformedURLException mue) {
      if (debugModeValue) {
        logger.severe(mue.getMessage());
      }
    } catch (IOException ioe) {
      if (debugModeValue) {
        logger.severe(ioe.getMessage());
      }
    }

    return response;
  }

  public String getUrlApi() {
    return urlApi;
  }

  public void setUrlApi(String urlApi) {
    this.urlApi = urlApi;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}
