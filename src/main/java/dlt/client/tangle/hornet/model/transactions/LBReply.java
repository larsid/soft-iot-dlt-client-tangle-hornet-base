package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class LBReply extends TargetedTransaction {

  public LBReply(String source, String group, String target) {
    super(source, group, TransactionType.LB_ENTRY_REPLY, target);
  }
}
