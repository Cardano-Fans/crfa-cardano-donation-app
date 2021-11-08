package crfa.app.repository;

import crfa.app.service.AppEnvService;
import io.blockfrost.sdk.api.BlockService;
import io.blockfrost.sdk.api.TransactionService;
import io.blockfrost.sdk.api.exception.APIException;
import io.blockfrost.sdk.api.model.Block;
import io.blockfrost.sdk.api.util.ConfigHelper;
import io.blockfrost.sdk.impl.BlockServiceImpl;
import io.blockfrost.sdk.impl.TransactionServiceImpl;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static crfa.app.infrastructure.AppEnv.MAINNET;
import static io.blockfrost.sdk.api.util.Constants.BLOCKFROST_MAINNET_URL;
import static io.blockfrost.sdk.api.util.Constants.BLOCKFROST_TESTNET_URL;

@Singleton
@Slf4j
public class BlockfrostApi {

    private final BlockService blockService;
    private final TransactionService transactionService;

    public BlockfrostApi(AppEnvService appEnvService, @Value("${blockFrostProjectId}") String projectId) {
        if (appEnvService.appEnv() == MAINNET) {
            log.info("Blockfrost API in mainnet mode, projectId:{}", projectId);
            this.blockService = new BlockServiceImpl(BLOCKFROST_MAINNET_URL, projectId);
            this.transactionService = new TransactionServiceImpl(BLOCKFROST_MAINNET_URL, projectId);
        } else {
            log.info("Blockfrost API in testnet mode, projectId:{}", projectId);
            this.blockService = new BlockServiceImpl(BLOCKFROST_TESTNET_URL, projectId);
            this.transactionService = new TransactionServiceImpl(BLOCKFROST_TESTNET_URL, projectId);
        }

        ConfigHelper.INSTANCE.setRateLimitForPeriod(1);
        ConfigHelper.INSTANCE.setThreadCount(1);
    }

    public Block getLatestBlock() {
        try {
            return blockService.getLatestBlock();
        } catch (APIException e) {
            log.error("Blockfrost error", e);
            throw new RuntimeException("blockfrost error", e);
        }
    }

}
