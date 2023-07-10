package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import net.minecraft.entity.LivingEntity;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;

import java.util.EnumSet;

@EntityDataIdentifier("FLAGS")
public final class OnFireMetadataTranslator extends EntityMetadataTranslator<EnumSet<EntityFlag>> {
    @Override
    public void translate(EntityMetadata<EnumSet<EntityFlag>> data) {
        var value = data.value().contains(EntityFlag.ON_FIRE);

        if (data.entity() instanceof LivingEntity livingEntity)
            livingEntity.setOnFire(value);

    }
}
