package br.uefs.larsid.iot.soft.model.transactions;

import br.uefs.larsid.iot.soft.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class Transaction {

  private final String source;
  private final String group;
  private final TransactionType type;

  private long createdAt;
  private long publishedAt;

  public Transaction(String source, String group, TransactionType type) {
    this.source = source;
    this.type = type;
    this.group = group;
    this.createdAt = System.currentTimeMillis();
  }

  public final String getSource() {
    return this.source;
  }

  public final String getGroup() {
    return this.group;
  }

  public final TransactionType getType() {
    return this.type;
  }

  public final long getCreatedAt() {
    return this.createdAt;
  }

  public final void setPublishedAt(long publishedAt) {
    this.publishedAt = publishedAt;
  }

  public final long getPublishedAt() {
    return this.publishedAt;
  }

  @Override
  public String toString() {
    return new StringBuilder("Transaction: ")
      .append(this.source)
      .append(this.group)
      .append(this.type)
      .append(this.createdAt)
      .append(this.publishedAt)
      .toString();
  }
}
