package crfa.app.service;

import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.Result;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import crfa.app.domain.Cadence;
import crfa.app.domain.Donation;
import crfa.app.repository.DonationRepository;
import crfa.app.repository.EntityRepository;
import crfa.app.repository.SuperEpochRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.*;

import static crfa.app.domain.Cadence.EPOCH;
import static crfa.app.domain.Cadence.SUPER_EPOCH;
import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

@Singleton
@Slf4j
public class EligibleDonationsService {

    @Value("${donation.cadence:SUPER_EPOCH}")
    private String donationCadence;

    @Inject
    private SuperEpochRepository superEpochRepository;
    @Inject

    private DonationRepository donationRepository;

    @Inject
    private EntityRepository entityRepository;

    @Inject
    private BlockService blockService;
    
    @Inject
    private Environment environment;

    public Optional<ImmutableList<Donation>> find() {
        Optional<Map<String, String>> donationConfig = environment.get("donation.entities", Argument.mapOf(String.class, String.class));
        if (donationConfig.isEmpty()) {
            log.debug("No donation entity configuration found");
            return Optional.empty();
        }

        ImmutableList.Builder<Donation> donationsBuilder = ImmutableList.builder();

        Cadence cadence = Enums.getIfPresent(Cadence.class, donationCadence).or(SUPER_EPOCH);

        int currentEpochNo = getCurrentEpochNo();
        int currentSuperEpochNo = superEpochRepository.currentSuperEpoch(currentEpochNo);

        donationConfig.get().forEach((entityId, donationInAdaStr) -> {
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
        ImmutableList<Donation> donations = donationsBuilder.build();

        return donations.isEmpty() ? Optional.empty() : Optional.of(donations);
    }

    private int getCurrentEpochNo() {
        try {
            Result<Block> result = blockService.getLastestBlock();
            if (result.isSuccessful()) {
                return result.getValue().getEpoch();
            }

            throw new RuntimeException("Failed to get latest block!");
        } catch (ApiException e) {
            log.error("Blockfrost error", e);
            throw new RuntimeException("blockfrost error", e);
        }
    }

}
