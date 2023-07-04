package lol.magix.breakingbedrock.translators.blockentity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import org.cloudburstmc.nbt.NbtMap;

public abstract class BlockEntityTranslator {
    public NbtCompound translateTag(NbtMap bedrockNbt) {
        return translateTag(bedrockNbt, getBaseCompoundTag(bedrockNbt));
    }

    public abstract NbtCompound translateTag(NbtMap bedrockNbt, NbtCompound newTag);

    public abstract BlockEntityType<?> getJavaId();

    protected NbtCompound getBaseCompoundTag(NbtMap bedrockNbt) {
        var tag = new NbtCompound();
        tag.putInt("x", bedrockNbt.getInt("x"));
        tag.putInt("y", bedrockNbt.getInt("y"));
        tag.putInt("z", bedrockNbt.getInt("z"));
        tag.putString("id", getJavaBlockEntityId(bedrockNbt.getString("id")));
        return tag;
    }

    protected String getJavaBlockEntityId(String bedrockBlockEntityId) {
        return switch (bedrockBlockEntityId) {
            // Some specific cases to handle
            // Also note: Chest can be trapped_chest
            case "EnchantTable" -> "minecraft:enchanting_table";
            case "JigsawBlock" -> "minecraft:jigsaw";
            case "PistonArm" -> "minecraft:piston_head";
            default -> {
                var builder = new StringBuilder("minecraft:");
                var index = 0; for (var c : bedrockBlockEntityId.toCharArray()) {
                    if (Character.isUpperCase(c) && index != 0) {
                        builder.append("_").append(Character.toLowerCase(c));
                    } else {
                        builder.append(c);
                    }
                    index++;
                }

                yield builder.toString();
            }
        };
    }
}
