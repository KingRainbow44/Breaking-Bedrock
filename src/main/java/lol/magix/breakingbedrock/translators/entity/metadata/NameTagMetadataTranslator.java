package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import net.minecraft.text.Text;

@EntityDataIdentifier("NAME")
public final class NameTagMetadataTranslator extends EntityMetadataTranslator<String> {
    @Override
    public void translate(EntityMetadata<String> data) {
        data.entity().setCustomName(Text.of(data.value()));
    }
}
