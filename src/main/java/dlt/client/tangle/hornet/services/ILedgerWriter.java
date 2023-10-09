package dlt.client.tangle.hornet.services;

import dlt.client.tangle.hornet.model.transactions.Transaction;

/**
 * @author Allan Capistrano
 * @version 1.0.0
 */
public interface ILedgerWriter {
  public void put(Transaction transaction) throws InterruptedException;

  public String getUrl();
}
