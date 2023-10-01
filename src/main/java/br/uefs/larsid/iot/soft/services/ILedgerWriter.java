package br.uefs.larsid.iot.soft.services;

import br.uefs.larsid.iot.soft.model.transactions.Transaction;

/**
 * @author Allan Capistrano
 * @version 1.0.0
 */
public interface ILedgerWriter {
  public void put(Transaction transaction) throws InterruptedException;

  public String getUrl();
}
