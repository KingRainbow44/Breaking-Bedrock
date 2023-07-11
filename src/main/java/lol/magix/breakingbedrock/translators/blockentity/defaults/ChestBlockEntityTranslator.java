package lol.magix.breakingbedrock.translators.blockentity.defaults;

import lol.magix.breakingbedrock.translators.blockentity.BlockEntityTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;

import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ChestBlockEntityTranslator extends BlockEntityTranslator {
    @Override
    public NbtCompound translateTag(NbtMap bedrockNbt, NbtCompound newTag) {
        this.putIfExists(bedrockNbt, newTag, "pairx", (Function<Integer, NbtElement>) NbtInt::of);
        this.putIfExists(bedrockNbt, newTag, "pairz", (Function<Integer, NbtElement>) NbtInt::of);

        var y = bedrockNbt.getInt("y");
        var xz = Vector2i.from(bedrockNbt.getInt("x"), bedrockNbt.getInt("z"));
        var blockPos = GameUtils.toBlockPos(Vector3i.from(xz.getX(), y, xz.getY()));
        if (bedrockNbt.containsKey("pairx", NbtType.INT) && bedrockNbt.containsKey("pairz", NbtType.INT)) {
            Vector2i pairXZ = Vector2i.from(bedrockNbt.getInt("pairx"), bedrockNbt.getInt("pairz"));

            var distance = xz.distance(pairXZ);
            if (distance != 1) {
                throw new IllegalStateException("Cannot this chest link from more or less blocks than 1");
            }
        }

        return newTag;
    }

    @SuppressWarnings("unchecked")
    private void putIfExists(NbtMap bedrockNbt, NbtCompound newTag, String key, Function<?, NbtElement> supplier) {
        if (bedrockNbt.containsKey(key)) {
            newTag.put(key, ((Function<Object, NbtElement>) supplier)
                    .apply(bedrockNbt.get(key)));
        }
    }

    @Override
    public BlockEntityType<?> getJavaId() {
        return BlockEntityType.CHEST;
    }
}
