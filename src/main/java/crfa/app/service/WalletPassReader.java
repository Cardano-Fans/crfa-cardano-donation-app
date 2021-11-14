package crfa.app.service;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
@Slf4j
public class WalletPassReader {

    @Value("${walletFilename:.crfa-donation-app-wallet.dat}")
    private String walletLocation;

    public String readWalletPass() {
        try {
            var file = new File(FileUtils.getUserDirectory(), walletLocation);
            log.info("Reading wallet from file: {}", file);

            return FileUtils.readFileToString(file, UTF_8);
        } catch (IOException e) {
            var msg = "Unable to read: " + walletLocation;
            log.error(msg, e);
            System.exit(-1);
            throw new RuntimeException(msg, e);
        }
    }

}
