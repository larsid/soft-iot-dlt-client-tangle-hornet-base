package br.uefs.larsid.iot.soft.model.tangle.hornet;

/**
 * @author Allan Capistrano
 * @version 1.0.0
 */
public class ApiMessage { // TODO: Alterar API para ser 'index' e 'data' e usar a classe 'Payload'

  private String index;
  private String content;

  public String getIndex() {
    return index;
  }

  public String getContent() {
    return content;
  }
}
