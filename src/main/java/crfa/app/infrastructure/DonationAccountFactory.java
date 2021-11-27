package crfa.app.infrastructure;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import crfa.app.service.AppEnvService;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Factory
@Slf4j
public class DonationAccountFactory {

    @Value("${walletFilename:.crfa-donation-app-wallet.dat}")
    private String walletPassFile;

    @Value("${walletIndex:0}")
    private int walletIndex;

    @Inject
    private AppEnvService appEnvService;

    @Bean
    public Account donationAccount() throws IOException {
        Network.ByReference network = appEnvService.isMainnet() ? Networks.mainnet() : Networks.testnet();
        String recoveryPhrase = readWalletPassFile(walletPassFile);
        return new Account(network, recoveryPhrase, walletIndex);
    }

    public String readWalletPassFile(String walletPassFile) throws IOException {
        try {
            var file = new File(FileUtils.getUserDirectory(), walletPassFile);
            log.info("Reading wallet from file: {}", file);

            return FileUtils.readFileToString(file, UTF_8);
        } catch (IOException e) {
            log.error("Unable to read wallet filename: {}", walletPassFile, e);
            throw e;
        }
    }

}
