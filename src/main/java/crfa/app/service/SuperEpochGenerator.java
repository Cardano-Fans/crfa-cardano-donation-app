package crfa.app.service;

import crfa.app.domain.SuperEpoch;
import io.vavr.collection.Stream;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static crfa.app.domain.SuperEpoch.START_EPOCH;
import static crfa.app.domain.SuperEpoch.STEP;

@Singleton
@Slf4j
public class SuperEpochGenerator {

    public List<SuperEpoch> getAllSuperEpochs(int upperEpochNo) {
        return Stream.rangeClosedBy(START_EPOCH, upperEpochNo, STEP)
                .map(i -> new SuperEpoch(i, i + (STEP - 1)))
                .collect(Collectors.toList());
    }

    public Optional<SuperEpoch> findSuperEpochByEpochNo(int epochNo, int upperEpochNo) {
        return getAllSuperEpochs(upperEpochNo).stream().filter(superEpoch -> superEpoch.containsEpoch(epochNo)).findFirst();
    }

    public Optional<SuperEpoch> findSuperEpochById(int superEpochNo, int upperEpochNo) {
        return getAllSuperEpochs(upperEpochNo).stream().filter(superEpoch -> superEpoch.getId() == superEpochNo).findFirst();
    }

}
