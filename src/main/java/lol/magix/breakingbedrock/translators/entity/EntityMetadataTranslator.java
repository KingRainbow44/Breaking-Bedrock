package lol.magix.breakingbedrock.translators.entity;

import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.translators.entity.metadata.*;
import net.minecraft.entity.Entity;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class EntityMetadataTranslator<T> {
    private static final Map<String, EntityMetadataTranslator<?>> TRANSLATORS = new HashMap<>();
    private static final List<EntityMetadataTranslator<EntityFlag>> FLAG_TRANSLATORS = new ArrayList<>();

    /**
     * Registers all metadata translators.
     */
    public static void initialize() {
        EntityMetadataTranslator.addTranslator(new AirMetadataTranslator());
        EntityMetadataTranslator.addTranslator(new ScaleMetadataTranslator());
        EntityMetadataTranslator.addTranslator(new HealthMetadataTranslator());
        EntityMetadataTranslator.addTranslator(new NameTagMetadataTranslator());
        EntityMetadataTranslator.addTranslator(new ImmobileMetadataTranslator());
        EntityMetadataTranslator.addTranslator(new SneakingMetadataTranslator());
        EntityMetadataTranslator.addTranslator(new NameTagVisibilityTranslator());
    }

    /**
     * Registers a translator.
     *
     * @param translator The translator to register.
     */
    public static void addTranslator(EntityMetadataTranslator<?> translator) {
        var type = translator.getClass();
        if (!type.isAnnotationPresent(EntityDataIdentifier.class))
            throw new IllegalArgumentException("Translator must have EntityDataIdentifier annotation.");

        var identifier = type.getAnnotation(EntityDataIdentifier.class).value();
        if (identifier.equals("FLAGS") || identifier.equals("FLAGS_2")) {
            FLAG_TRANSLATORS.add((EntityMetadataTranslator<EntityFlag>) translator);
        } else {
            TRANSLATORS.put(identifier, translator);
        }
    }

    /**
     * Translates the entity's metadata.
     *
     * @param data The entity and its data.
     */
    public static void translate(Pair<Entity, EntityDataMap> data) {
        var entity = data.a();
        var metadata = data.b();

        for (var entityData : metadata.keySet()) {
            var translator = TRANSLATORS.get(entityData.toString());
            if (translator == null) continue;

            try {
                translator.translate(new EntityMetadata<>(entity, metadata.get(entityData)));
            } catch (Exception exception) {
                BreakingBedrock.getLogger().warn("Unable to translate entity metadata.", exception);
            }
        }

        if (metadata.getFlags() != null) {
            for (var translator : FLAG_TRANSLATORS) try {
                translator.translate(new EntityMetadata<>(entity, metadata.getFlags()));
            } catch (Exception exception) {
                BreakingBedrock.getLogger().warn("Unable to translate entity metadata flags.", exception);
            }
        }
    }

    /**
     * Translates the entity's flags.
     *
     * @param data The entity and its flags.
     */
    private void translate(Object data) {
        this.translate((EntityMetadata<T>) data);
    }

    /**
     * Applies the data to the specified entity.
     *
     * @param data The data to apply.
     */
    public abstract void translate(EntityMetadata<T> data);
}
