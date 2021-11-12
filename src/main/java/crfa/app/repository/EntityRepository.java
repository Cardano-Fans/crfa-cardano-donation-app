package crfa.app.repository;

import com.google.common.collect.ImmutableMap;
import crfa.app.domain.Entity;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Singleton
@Slf4j
public class EntityRepository {

    // entityId to address
    private final Map<String, Entity> entityMap;

    public EntityRepository(Environment environment) {
        this.entityMap = loadEntities(environment);
    }

    public Optional<Entity> findById(String entityId) {
        return Optional.ofNullable(entityMap.get(entityId));
    }

    private Map<String, Entity> loadEntities(Environment environment) {
        ImmutableMap.Builder<String, Entity> builder = ImmutableMap.builder();

        environment.get("entities", Argument.mapOf(Argument.STRING, Argument.mapOf(String.class, String.class)))
                .ifPresent(list -> {
                        list.forEach((key, dataMap) -> {
                            dataMap.forEach((entityId, address) -> {
                                var entity = Entity.builder()
                                        .name(entityId)
                                        .address(address)
                                        .build();

                                builder.put(entityId, entity);
                            });
                        });
                });

        return builder.build();
    }

}
