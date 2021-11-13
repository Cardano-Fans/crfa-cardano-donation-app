package crfa.app.service;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
@Slf4j
public class WalletPassReader {

    private static final String CRFA_DONATION_APP_WALLET_DAT = ".crfa-donation-app-wallet.dat";

    public String readWalletPass() {
        try {
            var file = new File(FileUtils.getUserDirectory(), CRFA_DONATION_APP_WALLET_DAT);
            log.info("Reading wallet from file: {}", file);

            return FileUtils.readFileToString(file, UTF_8);
        } catch (IOException e) {
            var msg = "Unable to read " + CRFA_DONATION_APP_WALLET_DAT;
            log.error(msg, e);
            System.exit(-1);
            throw new RuntimeException(msg, e);
        }
    }

}
