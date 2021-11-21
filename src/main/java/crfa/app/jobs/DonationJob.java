package crfa.app.jobs;

import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Cadence;
import crfa.app.domain.Donation;
import crfa.app.repository.BlockfrostApi;
import crfa.app.repository.DonationRepository;
import crfa.app.repository.EntityRepository;
import crfa.app.repository.SuperEpochRepository;
import crfa.app.service.CardanoTokenSender;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

import static crfa.app.domain.Cadence.EPOCH;
import static crfa.app.domain.Cadence.SUPER_EPOCH;
import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

@Singleton
@Slf4j
public class DonationJob {

    private final BlockfrostApi blockfrostApi;
    private final SuperEpochRepository superEpochRepository;
    private final DonationRepository donationRepository;
    private final EntityRepository entityRepository;
    private final CardanoTokenSender cardanoTokenSender;
    private final Environment environment;

    @Value("${dryRunMode:true}")
    private boolean dryRunMode;

    @Value("${projectId}")
    private String projectId;

    @Value("${walletIndex:0}")
    private int walletIndex;

    public DonationJob(BlockfrostApi blockfrostApi,
                       SuperEpochRepository superEpochRepository,
                       DonationRepository donationRepository,
                       EntityRepository entityRepository,
                       CardanoTokenSender cardanoTokenSender,
                       Environment environment) {
        this.blockfrostApi = blockfrostApi;
        this.superEpochRepository = superEpochRepository;
        this.donationRepository = donationRepository;
        this.entityRepository = entityRepository;
        this.cardanoTokenSender = cardanoTokenSender;
        this.environment = environment;
    }

    @Scheduled(fixedDelay = "1h", initialDelay = "5s")
    public void onScheduled() {
        int currentEpochNo = blockfrostApi.getLatestBlock().getEpoch();

        int currentSuperEpochNo = superEpochRepository.currentSuperEpoch(currentEpochNo);

        ImmutableList.Builder<Donation> donationsBuilder = ImmutableList.builder();

        environment.get("donation.cadence", Argument.STRING).flatMap(cad -> Enums.getIfPresent(Cadence.class, cad).toJavaUtil()).ifPresent(cadence -> {
            environment.get("donation.entities", Argument.mapOf(String.class, String.class)).ifPresent(donationEntitiesMap -> {
                donationEntitiesMap.forEach((entityId, donationInAdaStr) -> {
                    if (isCreatable(donationInAdaStr) && Long.parseLong(donationInAdaStr) > 0) {
                        var adaDonation = Long.parseLong(donationInAdaStr);
                        entityRepository.findById(entityId).ifPresent(entity -> {

                            var donationToDo = false;
                            if (cadence == EPOCH && donationRepository.findDonationByEpochNo(currentEpochNo, entityId).isEmpty()) {
                                donationToDo = true;
                            } else if (cadence == SUPER_EPOCH && donationRepository.findDonationBySuperEpochNo(currentSuperEpochNo, entityId).isEmpty()) {
                                donationToDo = true;
                            }

                            if (donationToDo) {
                                var donation = Donation.builder()
                                        .id(UUID.randomUUID().toString())
                                        .address(entity.getAddress())
                                        .entityId(entityId)
                                        .amount(BigInteger.valueOf(adaDonation).multiply(BigInteger.valueOf(1_000_000L)))
                                        .cadence(cadence)
                                        .date(new Date())
                                        .superEpochNo(currentSuperEpochNo)
                                        .epochNo(currentEpochNo)
                                        .build();

                                donationsBuilder.add(donation);
                            }
                        });
                    }
                });

                var donations = donationsBuilder.build();

                // perform transaction to multiple parties
                try {
                    if (dryRunMode) {
                        log.info("Running in dryRun mode, we will not send ADA!");
                        donations.forEach(donation -> donation.setTransactionId("dry_run_mode"));
                    } else {
                        var transactionId = cardanoTokenSender.sendDonations(donations);
                        donations.forEach(donation -> donation.setTransactionId(transactionId));
                    }

                    donations.forEach(donationRepository::insertDonation);
                } catch (ApiException e) {
                    log.error("Sending of donations failed, reason:", e);
                }
            });
        });
    }

}
