package crfa.app.infrastructure;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.backend.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.backend.factory.BackendFactory;
import crfa.app.service.AppEnvService;
import io.blockfrost.sdk.api.util.ConfigHelper;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import static io.blockfrost.sdk.api.util.Constants.BLOCKFROST_MAINNET_URL;
import static io.blockfrost.sdk.api.util.Constants.BLOCKFROST_TESTNET_URL;

@Factory
@Slf4j
public class BlockchainServicesFactory {

    @Value("${projectId}")
    private String projectId;

    @Inject
    private AppEnvService appEnvService;

    static {
        ConfigHelper.INSTANCE.setRateLimitForPeriod(1);
        ConfigHelper.INSTANCE.setThreadCount(1);
    }

    @Bean
    public BackendService backendService() {
        return BackendFactory.getBlockfrostBackendService(blockfrostUrl(), projectId);
    }

    @Bean
    public BlockService blockService() {
        return backendService().getBlockService();
    }

    @Bean
    public FeeCalculationService feeCalculationService() {
        return backendService().getFeeCalculationService();
    }

    @Bean
    public TransactionHelperService transactionHelperService() {
        return backendService().getTransactionHelperService();
    }

    @Bean
    public String blockfrostUrl() {
        log.info("Blockfrost API: mode:{}, projectId:{}", appEnvService.appEnv(), projectId);
        return appEnvService.isMainnet() ? BLOCKFROST_MAINNET_URL : BLOCKFROST_TESTNET_URL;
    }

}
