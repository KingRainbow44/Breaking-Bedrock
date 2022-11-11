package lol.magix.breakingbedrock.translators;

import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.Resources;
import lol.magix.breakingbedrock.objects.game.BedrockBlockState;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Mappings for Bedrock to Java block palettes.
 */
public final class BlockPaletteTranslator {
    // Block IDs. Updated when loaded.
    public static int AIR_BLOCK_ID = 0;
    public static int WATER_BLOCK_ID = 9;

    @Getter private static final Object2IntMap<String> bedrockStateToRuntime = new Object2IntOpenHashMap<>();
    @Getter private static final Object2IntMap<BlockState> javaStateToRuntime = new Object2IntOpenHashMap<>();
    @Getter private static final Int2ObjectMap<BlockState> runtimeToState = new Int2ObjectOpenHashMap<>();

    /**
     * Loads mappings from the mappings file.
     */
    public static void loadMappings() {
        try (var blockPalette = ResourceUtils.getResourceAsStream(Resources.BLOCK_PALETTE)) {
            NbtList<NbtMap> blocks = null;
            try (var nbtStream = new NBTInputStream(new DataInputStream(new GZIPInputStream(blockPalette)))) {
                var palette = (NbtMap) nbtStream.readTag();
                blocks = (NbtList<NbtMap>) palette.getList("blocks", NbtType.COMPOUND);
            }

            // Check if the block palette was loaded.
            if (blocks == null) {
                BreakingBedrock.getLogger().error("Block palette could not be loaded.");
                return;
            }

            BlockPaletteTranslator.loadMappings(blocks);
        } catch (IOException ignored) { }
    }

    /**
     * Loads mappings from the NBT data.
     * @param data The NBT data.
     */
    private static void loadMappings(NbtList<NbtMap> data) {
        var runtimeId = 0;

        for (var map : data) {
            // Create mapping to runtime IDs.
            var blockState = BlockPaletteTranslator.getBlockState(map);
            bedrockStateToRuntime.put(blockState.toString(), runtimeId);

            // Create mapping to Java block states.
            var translatedBlockState = BlockStateTranslator.getBlockState(blockState.toString());
            if (translatedBlockState == null) {
                BreakingBedrock.getLogger().warn("Invalid block state for " + blockState);
                runtimeToState.put(runtimeId, Blocks.STONE.getDefaultState());
            } else {
                // Add bindings to associated properties.
                runtimeToState.put(runtimeId, translatedBlockState);
                javaStateToRuntime.put(translatedBlockState, runtimeId);

                // Check for special identifiers.
                switch (blockState.getIdentifier()) {
                    case "minecraft:air" -> AIR_BLOCK_ID = runtimeId;
                    case "minecraft:water" -> WATER_BLOCK_ID = runtimeId;
                }
            }

            // Increment runtime ID.
            runtimeId++;
        }

        BreakingBedrock.getLogger().info("Mapped {} block state palettes.", runtimeId);
    }

    /**
     * Returns the Bedrock block ID from a block state.
     * @param blockState The block state.
     * @return The Bedrock block ID.
     */
    public static int getBlockId(BedrockBlockState blockState) {
        return bedrockStateToRuntime.getOrDefault(blockState.toString(), AIR_BLOCK_ID);
    }

    /**
     * Returns the Bedrock block state from an NBT map.
     * @param map The NBT map.
     * @return The Bedrock block state.
     */
    public static BedrockBlockState getBlockState(NbtMap map) {
        var blockName = map.getString("name");
        var blockStates = map.getCompound("states");

        var blockState = new BedrockBlockState();
        blockState.setIdentifier(blockName);

        for (var state : blockStates.entrySet()) {
            var stateValue = state.getValue();

            var value = "";
            if (stateValue instanceof String || stateValue instanceof Integer) {
                value = stateValue.toString();
            } else if (stateValue instanceof Byte) {
                var theByte = (byte) stateValue;
                value = theByte == 0 ? "false" : "true";
            } else {
                BreakingBedrock.getLogger().warn("Invalid block state value " + stateValue);
            }

            blockState.getProperties().put(state.getKey(), value);
        }

        return blockState;
    }
}
