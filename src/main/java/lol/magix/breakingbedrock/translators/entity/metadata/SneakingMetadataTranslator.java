package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import net.minecraft.entity.EntityPose;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;

import java.util.EnumSet;

@EntityDataIdentifier("FLAGS")
public final class SneakingMetadataTranslator extends EntityMetadataTranslator<EnumSet<EntityFlag>> {
    @Override
    public void translate(EntityMetadata<EnumSet<EntityFlag>> data) {
        var value = data.value().contains(EntityFlag.SNEAKING);
        var entity = data.entity();

        entity.setSneaking(value);
        entity.setPose(value ?
                EntityPose.CROUCHING :
                EntityPose.STANDING);
    }
}
