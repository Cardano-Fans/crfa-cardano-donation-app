package crfa.app.service;

import crfa.app.infrastructure.AppEnv;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import static crfa.app.infrastructure.AppEnv.MAINNET;

@Singleton
public class AppEnvService {

    private String env;

    public AppEnvService(@Value("${env:testnet}") String env) {
        this.env = env;
    }

    public AppEnv appEnv() {
        return AppEnv.valueOf(env.toUpperCase());
    }

    public boolean isMainnet() {
        return MAINNET == appEnv();
    }
}
