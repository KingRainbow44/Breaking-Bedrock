package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;

@EntityDataIdentifier("NAMETAG_ALWAYS_SHOW")
public final class NameTagVisibilityTranslator extends EntityMetadataTranslator<Byte> {
    @Override
    public void translate(EntityMetadata<Byte> data) {
        data.entity().setCustomNameVisible(data.value() == 1);
    }
}
