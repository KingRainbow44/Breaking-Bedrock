package lol.magix.breakingbedrock.translators.blockstate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lol.magix.breakingbedrock.objects.absolute.Resources;
import lol.magix.breakingbedrock.objects.game.GeneralBlockState;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import lombok.Getter;
import net.minecraft.block.BlockState;

public final class LegacyBlockPaletteTranslator {
    @Getter private static final Int2ObjectMap<BlockState> legacyToId
            = new Int2ObjectOpenHashMap<>();
    @Getter private static final Object2IntMap<String> idToRuntime
            = new Object2IntOpenHashMap<>();

    /**
     * Loads mappings from the mappings file.
     */
    public static void loadMappings() {
        {
            // Load legacy block ID mappings.
            var mappings = ResourceUtils.getResourceAsObject(
                    Resources.LEGACY_BLOCK_IDS, JsonObject.class);
            if (mappings == null) return;

            // Load legacy block data mappings.
            var data = ResourceUtils.getResourceAsObject(
                    Resources.LEGACY_BLOCK_DATA, JsonArray.class);
            if (data == null) return;

            var blockRuntimeId = 0;
            for (var element : data) {
                var runtimeId = blockRuntimeId++; // Increment runtime ID.

                var blockState = BlockPaletteTranslator.getRuntime2Bedrock().get(runtimeId);
                var name = blockState.toString(false);
                var mapping = mappings.get(name);
                if (mapping == null) continue;

                var id = mapping.getAsInt();
                var metadata = element.getAsInt();

                var legacyId = id << 6 | metadata;
                legacyToId.put(legacyId, BlockStateTranslator.getRuntime2Java().get(runtimeId));
            }
        }

        {
            // Load legacy block ID mappings.
            var mappings = ResourceUtils.getResourceAsObject(
                    Resources.LEGACY_JAVA, JsonObject.class);
            if (mappings == null) return;

            for (var entry : mappings.entrySet()) {
                var legacy = entry.getKey();
                var javaState = new GeneralBlockState(
                        entry.getValue().getAsString());

                // Look-up the Java state for the runtime ID.
                var runtimeId = BlockStateTranslator.getJava2Runtime()
                        .getInt(javaState.toJavaBlockState());
                if (runtimeId <= 0) continue;

                idToRuntime.put(legacy, runtimeId);
            }
        }
    }
}
