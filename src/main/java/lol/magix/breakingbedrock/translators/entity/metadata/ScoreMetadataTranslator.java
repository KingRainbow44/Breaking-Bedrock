package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.objects.game.ScoreNameEntity;
import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.utils.TextUtils;

@EntityDataIdentifier("SCORE") // EntityDataTypes.SCORE
public final class ScoreMetadataTranslator extends EntityMetadataTranslator<String> {
    @Override
    public void translate(EntityMetadata<String> data) {
        var entity = data.entity();
        var value = data.value();

        if (entity instanceof ScoreNameEntity mixinEntity) {
            mixinEntity.setScoreName(value.isEmpty() ?
                    null : TextUtils.translate(data.value()));
        }
    }
}
