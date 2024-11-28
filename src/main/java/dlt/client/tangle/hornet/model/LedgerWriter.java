package dlt.client.tangle.hornet.model;

import com.google.gson.Gson;
import dlt.client.tangle.hornet.model.transactions.IndexTransaction;
import dlt.client.tangle.hornet.services.ILedgerWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
import static java.net.HttpURLConnection.HTTP_USE_PROXY;
import static java.net.HttpURLConnection.HTTP_VERSION;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    private static final String ENDPOINT = "api/v1/messages";

    public LedgerWriter(String protocol, String url, int port, int bufferSize) {
        this.urlApi = String.format("%s://%s:%s", protocol, url, port);
        this.DLTOutboundBuffer
                = new ArrayBlockingQueue<IndexTransaction>(bufferSize);
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

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format(
                    "{\"index\": \"%s\",\"data\": %s}",
                    index,
                    data
            );
            
            if (debugModeValue) {
                logger.log(Level.INFO, "Published message: {0}", requestBody);
            }
            
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(requestBody);
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();

            handleResponse(connection.getInputStream(), responseCode);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred while sending HTTP request: {0}", e.getMessage());
        }
    }

    private void handleResponse(InputStream stream, int responseCode) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String response = reader.lines().collect(Collectors.joining("\n"));

            if (responseCode >= HTTP_OK && responseCode <= HTTP_PARTIAL) {
                if (debugModeValue) {
                    logger.log(Level.INFO, "Successful API response: {0} - {1}", new Object[]{responseCode, response});
                }
            } else if (responseCode >= HTTP_MULT_CHOICE && responseCode <= HTTP_USE_PROXY) {
                if (debugModeValue) {
                    logger.log(Level.WARNING, "Redirection response: {0} - {1}", new Object[]{responseCode, response});
                }
            } else if (responseCode >= HTTP_BAD_REQUEST && responseCode <= HTTP_UNSUPPORTED_TYPE) {
                logger.log(Level.SEVERE, "Client error response: {0} - {1}", new Object[]{responseCode, response});
            } else if (responseCode >= HTTP_INTERNAL_ERROR && responseCode <= HTTP_VERSION) {
                logger.log(Level.SEVERE, "Server error response: {0} - {1}", new Object[]{responseCode, response});
            } else {
                logger.log(Level.WARNING, "Unknown response code: {0} - {1}", new Object[]{responseCode, response});
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception while reading response: {0}", e.getMessage());
        }
    }

}
