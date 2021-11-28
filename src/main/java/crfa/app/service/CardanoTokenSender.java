package crfa.app.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.backend.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.backend.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.backend.model.Result;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.transaction.model.PaymentTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Donation;
import crfa.app.service.transaction.TransactionRequestDto;
import crfa.app.service.transaction.TransactionRequestDtoFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

@Singleton
@Slf4j
@Setter
public class CardanoTokenSender {

    public static final int HEREAFTER_SLOT_COUNT = 1000;

    @Inject
    private TransactionRequestDtoFactory transactionRequestDtoFactory;

    @Inject
    private FeeCalculationService feeCalculationService;

    @Inject
    private BlockService blockService;

    @Inject
    private TransactionHelperService transactionHelperService;

    @Inject
    private Account donationAccount;

    public Result<TransactionResult> sendDonations(List<Donation> donations, String sentBy) {
        try {
            var ttl = blockService.getLastestBlock().getValue().getSlot() + HEREAFTER_SLOT_COUNT;

            TransactionRequestDto request = transactionRequestDtoFactory.create(donations, donationAccount, sentBy, ttl);

            ImmutableList<PaymentTransaction> paymentTransactions = request.getPaymentTransactions();
            TransactionDetailsParams detailsParams = request.getTransactionDetailsParams();
            Metadata metadata = request.getMetadata();

            var fee = feeCalculationService.calculateFee(paymentTransactions, detailsParams, metadata);

            // this can be confusing but actually we have to set transaction only once(!), if we set this for all
            // payment transactions we will pay 3 times
            applyFeeToFirstPaymentTransactionOnly(paymentTransactions, fee);

            return transactionHelperService.transfer(paymentTransactions, detailsParams, metadata);
        } catch (ApiException | AddressExcepion | CborSerializationException e) {
            log.error("Transaction failed", e);
            throw new RuntimeException("Transaction failed", e);
        }
    }

    private void applyFeeToFirstPaymentTransactionOnly(ImmutableList<PaymentTransaction> paymentTransactions, BigInteger fee) {
        paymentTransactions.stream().findFirst().ifPresent(paymentTransaction -> paymentTransaction.setFee(fee));
    }

}
