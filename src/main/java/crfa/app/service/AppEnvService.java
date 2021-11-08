package crfa.app.service;

import crfa.app.infrastructure.AppEnv;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class AppEnvService {

    @Value("${env:testnet}")
    private String env;

    public AppEnv appEnv() {
        return AppEnv.valueOf(env.toUpperCase());
    }

}
