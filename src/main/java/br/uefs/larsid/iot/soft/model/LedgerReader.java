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

  private static int HTTP_SUCCESS = 200;
  private String urlApi;
  private static final Logger logger = Logger.getLogger(
    LedgerReader.class.getName()
  );

  public void start() {
    this.getMessageByIndex("LB_ENTRY_REPLY");
  }

  public void stop() {
    logger.info("Finishing the soft-iot-dlt-client-tangle-hornet");
  }

  /**
   * Get all messages by a given index.
   *
   * @param index String - Message index
   */
  public void getMessageByIndex(String index) {
    String endpoint = "message";
    URL url;

    try {
      url = new URL(String.format("%s/%s/%s", this.urlApi, endpoint, index));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      if (conn.getResponseCode() != HTTP_SUCCESS) {
        throw new RuntimeException(
          "HTTP error code : " + conn.getResponseCode()
        );
      }

      BufferedReader br = new BufferedReader(
        new InputStreamReader((conn.getInputStream()))
      );

      String temp = null;
      String response = null;

      while ((temp = br.readLine()) != null) {
        response = temp;
      }

      conn.disconnect();

      logger.info(response);
    } catch (MalformedURLException mue) {
      logger.severe(mue.getMessage());
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
