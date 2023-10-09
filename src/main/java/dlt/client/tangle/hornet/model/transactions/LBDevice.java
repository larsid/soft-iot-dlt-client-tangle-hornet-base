package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author Antonio Crispim
 */
public class LBDevice extends TargetedTransaction {

  private final String device;

  public LBDevice(String source, String group, String device, String target) {
    super(source, group, TransactionType.LB_DEVICE, target);
    this.device = device;
  }

  public String getDevice() {
    return this.device;
  }
}
