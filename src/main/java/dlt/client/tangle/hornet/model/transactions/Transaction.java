package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;
import dlt.client.tangle.hornet.model.tangle.Payload;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Allan Capistrano, Uellington Damasceno
 * @version 1.0.0
 */
public class Transaction {

  private final String source;
  private final String group;
  private final TransactionType type;

  private long createdAt;
  private long publishedAt;
  private static final Logger logger = Logger.getLogger(
    Transaction.class.getName()
  );

  public Transaction(String source, String group, TransactionType type) {
    this.source = source;
    this.type = type;
    this.group = group;
    this.createdAt = System.currentTimeMillis();
  }

  /**
   * Get a transaction object by the field 'type' in the JSON.
   *
   * @param transactionJSON String - JSON in string.
   * @param debugModeValue boolean - Toggle log messages.
   * @return Transaction
   */
  public static Transaction getTransactionObjectByType(
    String transactionJSON,
    boolean debugModeValue
  ) {
    if (debugModeValue) {
      logger.info("JSON Message");
      logger.info(transactionJSON);
    }

    JsonReader reader = new JsonReader(new StringReader(transactionJSON));

    reader.setLenient(true);

    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

    String type = jsonObject.get("type").getAsString();
    Gson gson = new Gson();

    reader = new JsonReader(new StringReader(transactionJSON));
    reader.setLenient(true);

    if (type.equals(TransactionType.LB_ENTRY.name())) {
      return gson.fromJson(reader, Status.class);
    } else if (type.equals(TransactionType.LB_ENTRY_REPLY.name())) {
      return gson.fromJson(reader, LBReply.class);
    } else if (type.equals(TransactionType.LB_REPLY.name())) {
      return gson.fromJson(reader, Reply.class);
    } else if (type.equals(TransactionType.LB_REQUEST.name())) {
      return gson.fromJson(reader, Request.class);
    } else if (type.equals(TransactionType.LB_STATUS.name())) {
      return gson.fromJson(reader, Status.class);
    } else if (type.equals(TransactionType.REP_EVALUATION.name())) {
      return gson.fromJson(reader, Evaluation.class);
    } else {
      return gson.fromJson(reader, LBDevice.class);
    }
  }

  /**
   * Converts a JSON string into a Transaction object.
   *
   * @param string String - JSON in string.
   * @param debugModeValue boolean - Toggle log messages.
   * @return Transaction
   */
  public static Transaction jsonInStringToTransaction(
    String jsonInString,
    boolean debugModeValue
  ) {
    return Transaction.getTransactionObjectByType(
      Payload.stringToPayload(jsonInString).getData(),
      debugModeValue
    );
  }

  /**
   * Converts a JSON array string into a List of transactions objects.
   *
   * @param jsonArrayInString String - JSON array in string.
   * @param debugModeValue boolean - Toggle log messages.
   * @return List<Transaction>
   */
  public static List<Transaction> jsonArrayInStringToTransaction(
    String jsonArrayInString,
    boolean debugModeValue
  ) {
    Gson gson = new Gson();
    Type listType = new TypeToken<ArrayList<Payload>>() {}.getType();
    ArrayList<Payload> payloadList = gson.fromJson(jsonArrayInString, listType);
    List<Transaction> transactions = new ArrayList<Transaction>();

    payloadList.forEach(
      (
        payload -> {
          transactions.add(
            Transaction.getTransactionObjectByType(
              payload.getData(),
              debugModeValue
            )
          );
        }
      )
    );

    return transactions;
  }

  public final String getSource() {
    return this.source;
  }

  public final String getGroup() {
    return this.group;
  }

  public final TransactionType getType() {
    return this.type;
  }

  public final long getCreatedAt() {
    return this.createdAt;
  }

  public final void setPublishedAt(long publishedAt) {
    this.publishedAt = publishedAt;
  }

  public final long getPublishedAt() {
    return this.publishedAt;
  }

  @Override
  public String toString() {
    return new StringBuilder("Transaction: ")
      .append(this.source)
      .append(this.group)
      .append(this.type)
      .append(this.createdAt)
      .append(this.publishedAt)
      .toString();
  }
}
