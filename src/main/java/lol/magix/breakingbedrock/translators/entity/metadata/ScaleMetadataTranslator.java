package lol.magix.breakingbedrock.translators.entity.metadata;

import lol.magix.breakingbedrock.translators.entity.EntityDataIdentifier;
import lol.magix.breakingbedrock.translators.entity.EntityMetadata;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.utils.ReflectionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.lang.reflect.Field;

@EntityDataIdentifier("SCALE") // EntityDataTypes.SCALE
public final class ScaleMetadataTranslator extends EntityMetadataTranslator<Float> {
    private static final Field BLOCK_FIELD = ReflectionUtils.getField(FallingBlockEntity.class, "block");

    static {
        BLOCK_FIELD.setAccessible(true);
    }

    @Override
    public void translate(EntityMetadata<Float> data) {
        var isValid = data.value() < 1;
        var entity = data.entity();

        entity.setInvisible(isValid);
        if (entity instanceof FallingBlockEntity block) {
            try {
                BLOCK_FIELD.set(block, Blocks.BARRIER.getDefaultState());
            } catch (Exception ignored) { }
        } else if (entity instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY, Integer.MAX_VALUE,
                    0, false, false, false
            ));
        }
    }
}
