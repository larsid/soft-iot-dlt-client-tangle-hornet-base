package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author uellington
 */
public class LBMultiRequest extends Transaction{
 
  public LBMultiRequest(String source, String group) {
    super(source, group, TransactionType.LB_MULTI_REQUEST);
  }
  
}
