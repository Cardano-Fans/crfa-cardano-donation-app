package crfa.app.service;

import crfa.app.infrastructure.AppEnv;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class AppEnvService {

    private String env;

    public AppEnvService(@Value("${env:testnet}") String env) {
        this.env = env;
    }

    public AppEnv appEnv() {
        return AppEnv.valueOf(env.toUpperCase());
    }

}
