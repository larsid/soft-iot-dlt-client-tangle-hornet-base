package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class Request extends TargetedTransaction {

  private final String device;

  public Request(String source, String group, String device, String target) {
    super(source, group, TransactionType.LB_REQUEST, target);
    this.device = device;
  }

  public String getDevice() {
    return this.device;
  }
}
