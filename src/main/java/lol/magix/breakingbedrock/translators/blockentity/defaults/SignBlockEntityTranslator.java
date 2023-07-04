package lol.magix.breakingbedrock.translators.blockentity.defaults;

import lol.magix.breakingbedrock.translators.blockentity.BlockEntityTranslator;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import org.cloudburstmc.nbt.NbtMap;

public final class SignBlockEntityTranslator extends BlockEntityTranslator {
    @Override
    public NbtCompound translateTag(NbtMap bedrockNbt, NbtCompound newTag) {
        var text = bedrockNbt.getString("Text");
        String[] javaText = {"", "", "", ""};

        var textCount = 0;
        var builder = new StringBuilder();
        for (var c : text.toCharArray()) {
            if (c == '\n') {
                javaText[textCount++] = builder.toString();
                if (textCount > 3) {
                    break;
                }
                builder = new StringBuilder();
                continue;
            }
            builder.append(c);
        }

        for (var i = 0; i < javaText.length; i++) {
            System.out.println(javaText[i]);
        }

        return newTag;
    }

    @Override
    public BlockEntityType<?> getJavaId() {
        return BlockEntityType.SIGN;
    }
}
