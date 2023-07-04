package lol.magix.breakingbedrock.translators.blockentity.defaults;

import lol.magix.breakingbedrock.translators.blockentity.BlockEntityTranslator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import org.cloudburstmc.nbt.NbtMap;

@Data
@EqualsAndHashCode(callSuper = true)
public final class GenericBlockEntityTranslator extends BlockEntityTranslator {
    private final BlockEntityType<?> javaId;

    @Override
    public NbtCompound translateTag(NbtMap bedrockNbt, NbtCompound newTag) {
        return newTag;
    }
}
