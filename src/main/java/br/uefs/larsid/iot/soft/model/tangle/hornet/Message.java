package br.uefs.larsid.iot.soft.model.tangle.hornet;

import java.util.List;

/**
 * @author Allan Capistrano
 * @version 1.0.0
 */
public class Message {

  private String id;
  private long networkId;
  private int nonce;
  private List<String> parentMessageIds;
  private Payload payload;

  public String getId() {
    return id;
  }

  public long getNetworkId() {
    return networkId;
  }

  public int getNonce() {
    return nonce;
  }

  public List<String> getParentMessageIds() {
    return parentMessageIds;
  }

  public Payload getPayload() {
    return payload;
  }
}
