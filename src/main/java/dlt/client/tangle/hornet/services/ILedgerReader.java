package dlt.client.tangle.hornet.services;

import java.util.List;

import dlt.client.tangle.hornet.model.transactions.Transaction;

/**
 * @author Allan Capistrano, Uellington Damasceno
 * @version 1.0.0
 */
public interface ILedgerReader {
  public List<Transaction> getTransactionsByIndex(String index);

  public Transaction getTransactionById(String messageId);

  public void subscribe(String topic, ILedgerSubscriber subscriber);

  public void unsubscribe(String topic, ILedgerSubscriber subscriber);
}
