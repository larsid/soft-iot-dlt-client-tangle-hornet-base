package br.uefs.larsid.iot.soft.model.transactions;

import br.uefs.larsid.iot.soft.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class LBReply extends TargetedTransaction {

  public LBReply(String source, String group, String target) {
    super(source, group, TransactionType.LB_ENTRY_REPLY, target);
  }
}
