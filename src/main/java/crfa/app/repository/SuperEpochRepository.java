package crfa.app.repository;

import crfa.app.service.SuperEpochGenerator;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class SuperEpochRepository {

    private final SuperEpochGenerator superEpochGenerator;

    public SuperEpochRepository(SuperEpochGenerator superEpochGenerator) {
        this.superEpochGenerator = superEpochGenerator;
    }

    public int currentSuperEpoch(int epochNo) {
        return superEpochGenerator.getAllSuperEpochs(epochNo + 6)
                .stream().filter(superEpoch -> superEpoch.containsEpoch(epochNo)).findFirst().get().getId();
    }

}
