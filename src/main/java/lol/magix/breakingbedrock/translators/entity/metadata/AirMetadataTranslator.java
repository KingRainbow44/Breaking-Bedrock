package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;

@EntityDataIdentifier("AIR_SUPPLY") // EntityDataTypes.AIR_SUPPLY
public final class AirMetadataTranslator extends EntityMetadataTranslator<Short> {
    @Override
    public void translate(EntityMetadata<Short> data) {
        data.entity().setAir(data.value());
    }
}
