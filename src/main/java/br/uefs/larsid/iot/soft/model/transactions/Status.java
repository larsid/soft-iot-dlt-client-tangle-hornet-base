package br.uefs.larsid.iot.soft.model.transactions;

import br.uefs.larsid.iot.soft.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class Status extends Transaction {

  private final double avgLoad;
  private final double lastLoad;
  private final boolean available;

  public Status(
    String source,
    String group,
    boolean lbEntry,
    double avgLoad,
    double lastLoad,
    boolean available
  ) {
    super(
      source,
      group,
      lbEntry ? TransactionType.LB_ENTRY : TransactionType.LB_STATUS
    );
    this.avgLoad = avgLoad;
    this.lastLoad = lastLoad;
    this.available = available;
  }

  public double getAvgLoad() {
    return avgLoad;
  }

  public double getLastLoad() {
    return lastLoad;
  }

  public boolean getAvailable() {
    return available;
  }
}
