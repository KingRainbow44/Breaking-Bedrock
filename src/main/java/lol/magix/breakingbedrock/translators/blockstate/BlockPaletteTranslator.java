package lol.magix.breakingbedrock.translators.blockstate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.absolute.Resources;
import lol.magix.breakingbedrock.objects.game.GeneralBlockState;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import lombok.Getter;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleBlockDefinition;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;

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

    @Getter private static final Object2IntMap<GeneralBlockState> bedrock2Runtime = new Object2IntOpenHashMap<>();
    @Getter private static final Int2ObjectMap<GeneralBlockState> runtime2Bedrock = new Int2ObjectOpenHashMap<>();

    /**
     * Loads mappings from the mappings file.
     */
    public static void loadMappings() {
        try (var blockPalette = ResourceUtils.getResourceAsStream(Resources.BLOCK_PALETTE)) {
            NbtList<NbtMap> blocks; try (var nbtStream = new NBTInputStream(
                    new DataInputStream(new GZIPInputStream(blockPalette)))) {
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
        var registry = new SimpleDefinitionRegistry
                .Builder<BlockDefinition>();

        for (var block : data) {
            // Create mapping to runtime IDs.
            var blockState = new GeneralBlockState(block);
            bedrock2Runtime.put(blockState, runtimeId);
            runtime2Bedrock.put(runtimeId, blockState);

            // Check for special identifiers.
            switch (blockState.getIdentifier()) {
                case "air" -> AIR_BLOCK_ID = runtimeId;
                case "water" -> WATER_BLOCK_ID = runtimeId;
            }

            // Add the block to the registry.
            var builder = block.toBuilder();
            builder.remove("name_hash");
            builder.remove("network_id");

            block = builder.build();
            registry.add(new SimpleBlockDefinition(
                    block.getString("name"), runtimeId, block
            ));

            // Increment runtime ID.
            runtimeId++;
        }

        // Set the registry.
        GameConstants.BLOCKS.set(registry.build());

        BreakingBedrock.getLogger().info("Mapped {} block state palettes.", runtimeId);
    }

    /**
     * Returns the Bedrock block ID from a block state.
     * @param blockState The block state.
     * @return The Bedrock block ID.
     */
    public static int getBlockId(GeneralBlockState blockState) {
        var block = bedrock2Runtime.get(blockState);
        return block == null ? -1 : block;
    }
}
