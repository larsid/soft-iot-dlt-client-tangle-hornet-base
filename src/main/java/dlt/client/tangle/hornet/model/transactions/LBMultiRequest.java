package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author uellington
 */
public class LBMultiRequest extends TargetedTransaction{
    
  private final String device;

  public LBMultiRequest(String source, String group, String device, String target) {
    super(source, group, TransactionType.LB_MULTI_REQUEST, target);
    this.device = device;
  }

  public String getDevice() {
    return this.device;
  }
}
