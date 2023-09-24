package br.uefs.larsid.iot.soft.model.transactions;

import br.uefs.larsid.iot.soft.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class Reply extends TargetedTransaction {

  public Reply(String source, String group, String target) {
    super(source, group, TransactionType.LB_REPLY, target);
  }
}
