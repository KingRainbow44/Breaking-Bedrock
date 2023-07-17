package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.entity.mob.MobEntity;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;

import java.util.EnumSet;

@EntityDataIdentifier("FLAGS")
public final class ImmobileMetadataTranslator extends EntityMetadataTranslator<EnumSet<EntityFlag>> {
    @Override
    public void translate(EntityMetadata<EnumSet<EntityFlag>> data) {
        var value = data.value().contains(EntityFlag.NO_AI);

        var entity = data.entity();
        if (entity instanceof MobEntity mobEntity) {
            mobEntity.setAiDisabled(value);
        } else {
            GameUtils.setFlag(entity, 255, value);
        }

        entity.setNoGravity(value);
    }
}
