package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.objects.game.NameableEntity;
import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;

@EntityDataIdentifier("NAME")
public final class NameTagMetadataTranslator extends EntityMetadataTranslator<String> {
    @Override
    public void translate(EntityMetadata<String> data) {
        var entity = data.entity();
        var name = data.value();
        if (name.isEmpty()) return;

        if (entity instanceof NameableEntity mixinEntity) {
            mixinEntity.setName(name);
        }
    }
}
