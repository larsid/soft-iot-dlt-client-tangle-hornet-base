package dlt.client.tangle.hornet.model.transactions;

/**
 *
 * @author Allan Capistrano
 * @version 1.0.0
 */
public final class IndexTransaction {

    private final String index;
    private final Transaction transaction;

    public IndexTransaction(String index, Transaction transaction) {
        this.index = index;
        this.transaction = transaction;
    }

    public String getIndex() {
        return index;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
