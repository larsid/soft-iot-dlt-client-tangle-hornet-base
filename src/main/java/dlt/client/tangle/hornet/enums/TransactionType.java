package dlt.client.tangle.hornet.enums;

/**
 *
 * @author Uellington Damasceno, Allan Capistrano
 */
public enum TransactionType {
  LB_ENTRY(false),
  LB_ENTRY_REPLY(false),
  LB_STATUS(false),
  LB_REQUEST(false),
  LB_REPLY(false),
  LB_DEVICE(false),
  LB_MULTI_REQUEST(true),
  LB_MULTI_RESPONSE(true),
  LB_MULTI_DEVICE_REQUEST(true),
  LB_MULTI_DEVICE_RESPONSE(true);
  
  private final boolean multiLayer;
  
  private TransactionType(boolean multiLayer){
      this.multiLayer = multiLayer;
  }
  
  public boolean isMultiLayer(){
      return this.multiLayer;
  }
}
