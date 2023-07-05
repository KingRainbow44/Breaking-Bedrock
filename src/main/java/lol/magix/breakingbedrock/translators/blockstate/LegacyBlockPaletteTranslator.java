package lol.magix.breakingbedrock.translators.blockstate;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.absolute.Resources;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import lombok.Getter;
import net.minecraft.block.BlockState;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.util.stream.LittleEndianDataInputStream;

import java.io.IOException;

/**
 * Mappings for Bedrock (legacy blocks) to Java block palettes.
 */
public final class LegacyBlockPaletteTranslator {
    @Getter private static final Int2ObjectMap<BlockState> legacyToId = new Int2ObjectOpenHashMap<>();

    /**
     * Loads mappings from the mappings file.
     */
    public static void loadMappings() {
        NbtList<NbtMap> legacyBlockStates = null;

        // Read the legacy block states from the bindings file.
        try (var mappings = ResourceUtils.getResourceAsStream(Resources.LEGACY_BLOCKS_BINDINGS)) {
            try (var nbtStream = new NBTInputStream(new LittleEndianDataInputStream(mappings))) {
                //noinspection unchecked
                legacyBlockStates = (NbtList<NbtMap>) nbtStream.readTag();
            }
        } catch (IOException ignored) { }

        // Check if the legacy block states were loaded.
        Preconditions.checkNotNull(legacyBlockStates, "Legacy block states could not be loaded.");

        // Parse the NBT map.
        var runtimeId = -1;
        for (var nbt : legacyBlockStates) {
            runtimeId++; // Increment runtime ID.

            var states = nbt.getList("LegacyStates", NbtType.COMPOUND);
            if (states != null) for (var state : states) {
                var stateId = state.getInt("id") << 6 | state.getShort("val");
                legacyToId.put(stateId, BlockStateTranslator.getRuntime2Java().getOrDefault(
                                runtimeId, GameConstants.FALLBACK_BLOCK.getDefaultState()));
            }
        }

        BreakingBedrock.getLogger().info("Loaded {} legacy block state palettes.", legacyToId.size());
    }
}
