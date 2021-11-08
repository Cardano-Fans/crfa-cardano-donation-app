package crfa.app;

import io.blockfrost.sdk.api.util.NetworkHelper;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.ApplicationShutdownEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class CRFACardanoTokenDonationApp {

    public static void main(String[] args) {
        Micronaut.run(CRFACardanoTokenDonationApp.class, args);
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        log.info("Starting CRFATokenSenderApp...");
    }

    @EventListener
    public void stop(final ApplicationShutdownEvent event) {
        log.info("Stopping CRFATokenSenderApp...");
        NetworkHelper.getInstance().shutdown();
        log.info("Stopped.");
    }

}
