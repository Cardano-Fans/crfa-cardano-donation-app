package crfa.app.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class AppFactory {

    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
