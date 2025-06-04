package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class LBMultiResponse extends TargetedTransaction {

  public LBMultiResponse(String source, String group, String target) {
    super(source, group, TransactionType.LB_MULTI_RESPONSE, target);
  }
}
