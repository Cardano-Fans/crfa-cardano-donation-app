package crfa.app.service.transaction;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataList;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.transaction.model.PaymentTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Donation;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;

@Singleton
@Slf4j
public class TransactionRequestDtoFactory {

    public static final String MEMO_APP_NAME = "CRFA-Cardano-Donation-App";
    public static final String MEMO_SENT_BY = "Sent by: %s";
    public static final String MEMO_ATTRIBUTION = "Created by: Cardano Fans Staking Pool (ticker: CRFA).";
    public static final String CIP20_META_DATUM_LABEL = "674";
    public static final String METADATA_MESSAGES_KEY = "msg";

    public TransactionRequestDto create(List<Donation> donations, Account account, String sentBy, long ttl) {
        if (donations.isEmpty()) {
            throw new IllegalArgumentException("Nothing to send!");
        }
        var detailsParams =
                TransactionDetailsParams.builder()
                        .ttl(ttl)
                        .build();

        var paymentTransactionBuilder = ImmutableList.<PaymentTransaction>builder();
        for (Donation donation : donations) {
            log.debug("Donation:{}", donation);

            var paymentTransaction = PaymentTransaction.builder()
                    .sender(account)
                    .receiver(donation.getAddress())
                    .amount(donation.getAmount())
                    .unit(LOVELACE)
                    .build();

            paymentTransactionBuilder.add(paymentTransaction);
        }
        var paymentTransactions = paymentTransactionBuilder.build();

        var memoMetadata = createMemoMetadata(List.of(
                MEMO_APP_NAME,
                String.format(MEMO_SENT_BY, sentBy),
                MEMO_ATTRIBUTION
        ));

        return new TransactionRequestDto(paymentTransactions, detailsParams, memoMetadata);
    }

    // create metadata according to CIP: https://cips.cardano.org/cips/cip20/
    private Metadata createMemoMetadata(List<String> memos) {
        var cborMemos = new CBORMetadataList();

        memos.forEach(cborMemos::add);

        var memo = new CBORMetadataMap()
                .put(METADATA_MESSAGES_KEY, cborMemos);

        return new CBORMetadata()
                .put(new BigInteger(CIP20_META_DATUM_LABEL), memo);
    }

}