package crfa.app.service.transaction;

import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.transaction.model.PaymentTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

public class TransactionRequestDto {
    @Getter
    private final ImmutableList<PaymentTransaction> paymentTransactions;
    @Getter
    private final TransactionDetailsParams transactionDetailsParams;
    @Getter
    private final Metadata metadata;

    public TransactionRequestDto(ImmutableList<PaymentTransaction> paymentTransactions,
                                 TransactionDetailsParams transactionDetailsParams, Metadata metadata) {
        this.paymentTransactions = paymentTransactions;
        this.transactionDetailsParams = transactionDetailsParams;
        this.metadata = metadata;
    }
}