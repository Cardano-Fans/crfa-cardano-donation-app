package crfa.app.jobs;

import crfa.app.repository.BlockfrostApi;
import crfa.app.repository.DonationRepository;
import crfa.app.repository.EntityRepository;
import crfa.app.repository.SuperEpochRepository;
import io.micronaut.context.env.Environment;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DonationJob {

    private final BlockfrostApi blockfrostApi;
    private final SuperEpochRepository superEpochRepository;
    private final DonationRepository donationRepository;
    private final EntityRepository entityRepository;

    public DonationJob(BlockfrostApi blockfrostApi,
                       SuperEpochRepository superEpochRepository,
                       DonationRepository donationRepository,
                       EntityRepository entityRepository) {
        this.blockfrostApi = blockfrostApi;
        this.superEpochRepository = superEpochRepository;
        this.donationRepository = donationRepository;
        this.entityRepository = entityRepository;
    }

    @Scheduled(fixedDelay = "1h", initialDelay = "5s")
    public void onScheduled() {
        int currentEpochNo = blockfrostApi.getLatestBlock().getEpoch();

        int currentSuperEpochNo = superEpochRepository.currentSuperEpoch(currentEpochNo);

        //this.denyListedMap = (Map<String, String>) environment.get("entities", Map.class).get();

        // read yaml file... and then check if we already send donation based on cadence (either epoch or super epoch)

        //

    }

    //private void loadEntities()

}
