package crfa.app.repository;

import crfa.app.domain.SuperEpoch;
import crfa.app.service.SuperEpochGenerator;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Singleton
@Slf4j
public class SuperEpochRepository {

    private final BlockfrostApi blockfrostApi;
    private final SuperEpochGenerator superEpochGenerator;
    private List<SuperEpoch> superEpochs;

    public SuperEpochRepository(BlockfrostApi blockfrostApi,
                                SuperEpochGenerator superEpochGenerator) {
        this.blockfrostApi = blockfrostApi;
        this.superEpochGenerator = superEpochGenerator;
        refreshData();
    }

    public int currentSuperEpoch(int epochNo) {
        return getSuperEpochs().stream().filter(superEpoch -> superEpoch.containsEpoch(epochNo)).findFirst().get().getId();
    }

    public List<SuperEpoch> getSuperEpochs() {
        return superEpochs;
    }

    @Scheduled(fixedDelay = "1h", initialDelay = "1h")
    public void refreshData() {
        var epochNo = blockfrostApi.getLatestBlock().getEpoch();
        log.info("Refreshing super epochs... currentEpochNo: {}", epochNo);

        this.superEpochs = superEpochGenerator.getAllSuperEpochs(epochNo + 6);
    }

}
