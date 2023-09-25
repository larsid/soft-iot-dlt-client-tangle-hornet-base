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

  private String urlApi;
  private boolean debugModeValue;
  private static final Logger logger = Logger.getLogger(
    LedgerReader.class.getName()
  );

  public void start() {
    // TODO: Temporário, remover depois
    logger.info(this.getMessagesByIndex("LB_ENTRY_REPLY"));
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
  public String getMessagesByIndex(String index) {
    String endpoint = "message";
    URL url;
    String response = null;

    try {
      url = new URL(String.format("%s/%s/%s", this.urlApi, endpoint, index));
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
