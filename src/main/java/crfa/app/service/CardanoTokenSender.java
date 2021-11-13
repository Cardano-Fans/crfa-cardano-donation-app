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
import io.micronaut.retry.annotation.Retryable;
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

    @Value("${projectId}")
    private String projectId;

    @Value("${walletIndex:0}")
    private int walletIndex;

    public CardanoTokenSender(WalletPassReader walletPassReader,
                              AppEnvService appEnvService) {
        this.appEnvService = appEnvService;

        var pass = walletPassReader.readWalletPass();

        this.donationAccount = createAccount(pass, walletIndex);
    }

    @Retryable(attempts = "10", delay ="10s")
    public String sendDonations(List<Donation> donations) {
        if (donations.isEmpty()) {
            throw new RuntimeException("Nothing to send!");
        }

        try {
            var paymentTransactionBuilder = ImmutableList.<PaymentTransaction>builder();

            var ttl = blockService.getLastestBlock().getValue().getSlot() + 1000;
            for (Donation donation : donations) {
                var p = PaymentTransaction.builder()
                        .sender(donationAccount)
                        .receiver(donation.getAddress())
                        .amount(donation.getAmount())
                        .unit(LOVELACE)
                        .build();

                paymentTransactionBuilder.add(p);
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

            // TODO new version of library
            var fee
                    = feeCalculationService.calculateFee(paymentTransactions, detailsParams, memoMetadata);

            paymentTransactions.forEach(paymentTransaction -> paymentTransaction.setFee(fee));

            var transaction
                    = transactionHelperService.transfer(paymentTransactions, detailsParams, memoMetadata);

            if (transaction.isSuccessful()) {
                var transactionResult = (TransactionResult) transaction.getValue();
                log.info("Transaction was successful, trxId:{}", transactionResult);

                return transactionResult.getTransactionId();
            }
        } catch (ApiException e) {
            log.error("blockfrost error", e);
        } catch (AddressExcepion e) {
            log.error("address error", e);
        } catch (CborSerializationException e) {
            log.error("cbor serializtion error", e);
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
