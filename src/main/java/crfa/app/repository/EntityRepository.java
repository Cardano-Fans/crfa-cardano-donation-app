package crfa.app.repository;

import com.google.common.collect.ImmutableMap;
import crfa.app.domain.Entity;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Singleton
@Slf4j
public class EntityRepository {

    // entityId to address
    private Map<String, Entity> entityMap;

    public EntityRepository(Environment environment) {
        loadEntities(environment);
    }

    public Map<String, Entity> getEntityMap() {
        return entityMap;
    }

    private Map<String, Entity> loadEntities(Environment environment) {
        ImmutableMap.Builder<String, Entity> builder = ImmutableMap.builder();

        environment.get("entities", Map.class)
                .ifPresent(dataMap -> {
                    dataMap.values().forEach(a -> {
                        var obj = (Map) a;

                        var name = (String) obj.get("name");

                        var entity = Entity.builder()
                                .name(name)
                                .address((String) obj.get("address"))
                                .build();

                        builder.put(name, entity);
                    });
                });

        return builder.build();
    }

}
