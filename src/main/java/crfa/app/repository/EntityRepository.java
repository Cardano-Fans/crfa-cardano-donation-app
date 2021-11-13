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

        environment.get("entities", Argument.mapOf(Argument.STRING, Argument.mapOf(String.class, Object.class)))
                .ifPresent(list -> {
                        list.forEach((entityId, dataMap) -> {
                            dataMap.forEach((name, obj) -> {
                                if (obj instanceof String) {
                                    var addr  = (String) obj;
                                    if (name.equals("address")) {
                                        var entity = Entity.builder()
                                                .name(entityId)
                                                .address(addr)
                                                .build();

                                        builder.put(entityId, entity);
                                    }
                                }

                            });
                        });
                });

        return builder.build();
    }

}
