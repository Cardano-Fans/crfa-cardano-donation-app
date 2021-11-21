package crfa.app.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.backend.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.backend.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.backend.factory.BackendFactory;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataList;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.transaction.model.PaymentTransaction;
import com.bloxbean.cardano.client.transaction.model.TransactionDetailsParams;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Donation;
import crfa.app.infrastructure.AppEnv;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

import static com.bloxbean.cardano.client.backend.impl.blockfrost.common.Constants.BLOCKFROST_MAINNET_URL;
import static com.bloxbean.cardano.client.backend.impl.blockfrost.common.Constants.BLOCKFROST_TESTNET_URL;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static crfa.app.infrastructure.AppEnv.MAINNET;

@Singleton
@Slf4j
public class CardanoTokenSender {

    private final AppEnvService appEnvService;

    private FeeCalculationService feeCalculationService;
    private BlockService blockService;
    private TransactionHelperService transactionHelperService;

    private final Account donationAccount;

    private final String projectId;

    public CardanoTokenSender(WalletPassReader walletPassReader,
                              AppEnvService appEnvService,
                              @Value("${projectId}") String projectId,
                              @Value("${walletIndex:0}") int walletIndex) {
        this.appEnvService = appEnvService;
        this.projectId = projectId;

        var pass = walletPassReader.readWalletPass();

        this.donationAccount = createAccount(pass, walletIndex);
    }

    public String sendDonations(List<Donation> donations) throws ApiException {
        if (donations.isEmpty()) {
            throw new RuntimeException("Nothing to send!");
        }

        try {
            var paymentTransactionBuilder = ImmutableList.<PaymentTransaction>builder();

            var ttl = blockService.getLastestBlock().getValue().getSlot() + 1000;
            for (Donation donation : donations) {
                log.info("Donation:{}", donation);
                var paymentTransaction = PaymentTransaction.builder()
                        .sender(donationAccount)
                        .receiver(donation.getAddress())
                        .amount(donation.getAmount())
                        .unit(LOVELACE)
                        .build();

                paymentTransactionBuilder.add(paymentTransaction);
            }

            var detailsParams =
                    TransactionDetailsParams.builder()
                            .ttl(ttl)
                            .build();

            var paymentTransactions = paymentTransactionBuilder.build();

            var memoMetadata = createMemoMetadata(List.of(
                    "CRFA-Cardano-Donation-App",
                    "Cardano Fans Staking Pool (ticker: CRFA).",
                    "https://github.com/Cardano-Fans/crfa-cardano-donation-app"
            ));

            var fee
                    = feeCalculationService.calculateFee(paymentTransactions, detailsParams, memoMetadata);

            // this can be confusing but actually we have to set transaction only once(!), if we set this for all
            // payment transactions we will pay 3 times
            paymentTransactions.stream().findFirst().ifPresent(paymentTransaction -> paymentTransaction.setFee(fee));

            var transaction
                    = transactionHelperService.transfer(paymentTransactions, detailsParams, memoMetadata);

            if (transaction.isSuccessful()) {
                var transactionResult = (TransactionResult) transaction.getValue();
                log.info("Transaction was successful, trxId:{}", transactionResult);

                return transactionResult.getTransactionId();
            }

            log.error("Transaction failed, response:{}", transaction.getResponse());
        } catch (ApiException e) {
            log.error("Blockfrost error", e);
            throw e;
        } catch (AddressExcepion e) {
            log.error("Address error", e);
        } catch (CborSerializationException e) {
            log.error("CBOR serializtion error", e);
        }

        throw new RuntimeException("Transaction failed.");
    }

    private Account createAccount(String pass, int index) {
        if (appEnvService.appEnv() == MAINNET) {
            return new Account(Networks.mainnet(), pass, index);
        }

        return new Account(Networks.testnet(), pass, index);
    }

    private BackendService createBlockfrostBackend() {
        AppEnv appEnv = appEnvService.appEnv();

        log.info("Blockfrost in {} mode, projectId:{}", appEnv, projectId);

        if (appEnv == MAINNET) {
            return BackendFactory.getBlockfrostBackendService(BLOCKFROST_MAINNET_URL, projectId);
        }

        return BackendFactory.getBlockfrostBackendService(BLOCKFROST_TESTNET_URL, projectId);
    }

    // create metadata according to CIP: https://cips.cardano.org/cips/cip20/
    private Metadata createMemoMetadata(List<String> memos) {
        var cborMemos = new CBORMetadataList();

        memos.forEach(cborMemos::add);

        var memo = new CBORMetadataMap()
                .put("msg", cborMemos);

        return new CBORMetadata()
                .put(new BigInteger("674"), memo);
    }

    @EventListener
    public void onStartUp(ServerStartupEvent event) {
        var backendService = createBlockfrostBackend();

        this.feeCalculationService = backendService.getFeeCalculationService();
        this.blockService = backendService.getBlockService();
        this.transactionHelperService = backendService.getTransactionHelperService();
    }

}
