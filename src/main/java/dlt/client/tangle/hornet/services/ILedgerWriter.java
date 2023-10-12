package dlt.client.tangle.hornet.services;

import dlt.client.tangle.hornet.model.transactions.IndexTransaction;

/**
 * @author Allan Capistrano
 * @version 1.1.0
 */
public interface ILedgerWriter {
  public void put(IndexTransaction indexTransaction)
    throws InterruptedException;

  public String getUrl();
}
