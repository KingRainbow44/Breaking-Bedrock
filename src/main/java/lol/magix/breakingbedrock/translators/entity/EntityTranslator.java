package lol.magix.breakingbedrock.translators.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

public interface EntityTranslator {
    Map<String, EntityType<?>> bedrock2Entity = new HashMap<>() {{
        // These are some Bedrock states which aren't common with Java.
        this.put("minecraft:ender_crystal", EntityType.END_CRYSTAL);
        this.put("minecraft:splash_potion", EntityType.POTION);
    }};

    /**
     * Loads the mappings from the Minecraft enum.
     */
    static void loadMappings() {
        var types = Registries.ENTITY_TYPE.stream().toList();
        for (var type : types) {
            bedrock2Entity.put(
                    EntityType.getId(type).toString(), type);
        }
    }
}
