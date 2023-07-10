package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import net.minecraft.entity.LivingEntity;

@EntityDataIdentifier("HEALTH") // EntityDataTypes.HEALTH
public final class HealthMetadataTranslator extends EntityMetadataTranslator<Integer> {
    @Override
    public void translate(EntityMetadata<Integer> data) {
        if (data.entity() instanceof LivingEntity livingEntity)
            livingEntity.setHealth(data.value());
    }
}
