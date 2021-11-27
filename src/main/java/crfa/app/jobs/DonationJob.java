package crfa.app.jobs;

import com.bloxbean.cardano.client.backend.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.backend.model.Result;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Donation;
import crfa.app.repository.DonationRepository;
import crfa.app.service.CardanoTokenSender;
import crfa.app.service.EligibleDonationsService;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Singleton
@Slf4j
public class DonationJob {
    @Inject
    private DonationRepository donationRepository;
    @Inject
    private CardanoTokenSender cardanoTokenSender;

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    @Value("${donation.donor:anonymous}")
    private String donor;

    @Inject
    private EligibleDonationsService eligibleDonationsService;

    @Scheduled(fixedDelay = "1h", initialDelay = "5s")
    public void onScheduled() {
        Optional<ImmutableList<Donation>> optionalDonations = eligibleDonationsService.find();
        if (optionalDonations.isEmpty()) {
            log.warn("No eligible donations are found...");
            return;
        }
        ImmutableList<Donation> donations = optionalDonations.get();
        // perform transaction to multiple parties
        if (dryRunMode) {
            log.info("Running in dryRun mode, we will not send ADA!");
            donations.forEach(donation -> donation.setTransactionId("dry_run_mode"));
            donations.forEach(donationRepository::insertDonation);
        } else {
            Result<TransactionResult> txResponse = cardanoTokenSender.sendDonations(donations, donor);
            if (txResponse.isSuccessful()) {
                var transactionResult = (TransactionResult) txResponse.getValue();
                log.info("Transaction was successful, trxId:{}", transactionResult.getTransactionId());
                donations.forEach(donation -> donation.setTransactionId(transactionResult.getTransactionId()));
                donations.forEach(donationRepository::insertDonation);
            } else {
                log.error("Sending donations failed, txResponse: {}", txResponse);
            }
        }
    }

}
