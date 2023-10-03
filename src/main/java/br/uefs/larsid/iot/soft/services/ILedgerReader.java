package br.uefs.larsid.iot.soft.services;

/**
 * @author Allan Capistrano, Uellington Damasceno
 * @version 1.0.0
 */
public interface ILedgerReader {
  public String getMessagesByIndex(String index);

  public String getMessageByMessageId(String messageId);

  public void subscribe(String topic, ILedgerSubscriber subscriber);

  public void unsubscribe(String topic, ILedgerSubscriber subscriber);
}
