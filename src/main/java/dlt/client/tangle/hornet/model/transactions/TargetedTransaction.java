package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public abstract class TargetedTransaction extends Transaction {

  private final String target;

  public TargetedTransaction(
    String source,
    String group,
    TransactionType type,
    String target
  ) {
    super(source, group, type);
    this.target = target;
  }

  public final String getTarget() {
    return this.target;
  }
}
