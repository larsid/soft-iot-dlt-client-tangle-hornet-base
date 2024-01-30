package dlt.client.tangle.hornet.model.transactions;

import dlt.client.tangle.hornet.enums.TransactionType;

/**
 *
 * @author Uellington Damasceno
 */
public class LBMultiDevice extends TargetedTransaction {

    private final String device;

    public LBMultiDevice(String source, String group, String device, String target) {
        super(source, group, TransactionType.LB_MULTI_DEVICE_REQUEST, target);
        this.device = device;
    }

    public String getDevice() {
        return this.device;
    }
}
