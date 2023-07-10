package lol.magix.breakingbedrock.translators.blockstate;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds mappings from Java Edition block states to Bedrock Edition block states.
 */
public final class BlockStateTranslator {
    @Getter private static final Map<GeneralBlockState, GeneralBlockState> bedrock2Java = new HashMap<>();
    @Getter private static final Map<GeneralBlockState, GeneralBlockState> java2Bedrock = new HashMap<>();

    @Getter private static final Int2ObjectMap<BlockState> runtime2Java = new Int2ObjectOpenHashMap<>();
    @Getter private static final Object2IntMap<BlockState> java2Runtime = new Object2IntOpenHashMap<>();

    /**
     * Loads mappings from the mappings file.
     * This loads mappings in the following order:
     * - GeyserMC Java <-> Bedrock Mappings (recent)
     * - PrismarineJS Bedrock -> Java Mappings (1.19.1)
     * - PrismarineJS Bedrock <- Java Mappings (1.19.1)
     */
    public static void loadMappings() {
        {
            // Load Geyser's mappings.
            var mappings = ResourceUtils.getResourceAsObject(
                    Resources.BLOCKS_MAPPINGS, JsonObject.class);
            if (mappings == null) return;

            for (var entry : mappings.entrySet()) {
                var javaState = new GeneralBlockState(entry.getKey());
                var bedrockState = entry.getValue().getAsJsonObject();

                // Create a Bedrock block state mapping.
                var blockState = new GeneralBlockState();
                var name = bedrockState.get("bedrock_identifier").getAsString();
                blockState.setNamespace(name.substring(0, name.indexOf(":")));
                blockState.setIdentifier(name.substring(name.indexOf(":") + 1));

                // Check if the entry has additional states.
                if (bedrockState.has("bedrock_states")) {
                    for (var stateEntry : bedrockState.get("bedrock_states")
                            .getAsJsonObject().entrySet()) {
                        if (!(stateEntry.getValue() instanceof JsonPrimitive jsonValue)) continue;

                        String value; if (jsonValue.isString())
                            value = jsonValue.getAsString();
                        else if (jsonValue.isNumber())
                            value = String.valueOf(jsonValue.getAsNumber());
                        else if (jsonValue.isBoolean())
                            value = jsonValue.getAsBoolean() ? "1" : "0";
                        else {
                            BreakingBedrock.getLogger().warn("Invalid block state value.");
                            continue;
                        }

                        blockState.getProperties().put(stateEntry.getKey(), value);
                    }
                }

                // Fetch the runtime ID of the block.
                var bedrockRuntimeId = BlockPaletteTranslator.getBlockId(blockState);
                if (bedrockRuntimeId == -1) {
                    BreakingBedrock.getLogger().debug("Invalid Bedrock block state (blocks.json): {}", blockState);
                    continue;
                }

                java2Bedrock.put(javaState, blockState);
                java2Runtime.put(javaState.toJavaBlockState(), bedrockRuntimeId);

                // This is used for redundancy.
                bedrock2Java.put(blockState, javaState);
                runtime2Java.put(bedrockRuntimeId, javaState.toJavaBlockState());
            }
        }

        {
            // Load Prismarine Bedrock -> Java mappings.
            var mappings = ResourceUtils.getResourceAsObject(
                    Resources.BEDROCK_BLOCKS, JsonObject.class);
            if (mappings == null) return;

            for (var entry : mappings.entrySet()) {
                var bedrock = new GeneralBlockState(entry.getKey());
                var java = new GeneralBlockState(entry.getValue().getAsString());

                // Validate the block.
                if (java.toJavaBlock() == null) {
                    BreakingBedrock.getLogger().warn("Invalid Java block state: {}", java);
                    continue;
                }

                // Fetch the runtime ID of the block.
                var bedrockRuntimeId = BlockPaletteTranslator.getBlockId(bedrock);
                if (bedrockRuntimeId == -1) {
                    BreakingBedrock.getLogger().debug("Invalid Bedrock block state (b2j.json): {}", bedrock);
                    continue;
                }

                runtime2Java.put(bedrockRuntimeId, java.toJavaBlockState());
                bedrock2Java.put(bedrock, java);
            }
        }

        {
            // Load Prismarine Java -> Bedrock mappings.
            var mappings = ResourceUtils.getResourceAsObject(
                    Resources.JAVA_BLOCKS, JsonObject.class);
            if (mappings == null) return;

            for (var entry : mappings.entrySet()) {
                var java = new GeneralBlockState(entry.getKey());
                var bedrock = new GeneralBlockState(entry.getValue().getAsString());

                // Validate the block.
                if (java.toJavaBlock() == null) {
                    BreakingBedrock.getLogger().warn("Invalid Java block state: {}", java);
                    continue;
                }

                // Fetch the runtime ID of the block.
                var bedrockRuntimeId = BlockPaletteTranslator.getBlockId(bedrock);
                if (bedrockRuntimeId == -1) {
                    BreakingBedrock.getLogger().debug("Invalid Bedrock block state (j2b.json): {}", bedrock);
                    continue;
                }

                java2Runtime.put(java.toJavaBlockState(), bedrockRuntimeId);
                java2Bedrock.put(java, bedrock);
            }
        }

        BreakingBedrock.getLogger().info("Loaded {} block state mappings.", bedrock2Java.size());
    }

    /**
     * Parses the final property value from a string.
     * @param current The current block state.
     * @param property The property to parse.
     * @param value The value to parse.
     * @return The parsed block state.
     */
    public static <T extends Comparable<T>> BlockState parsePropertyValue(BlockState current, Property<T> property, String value) {
        var optional = property.parse(value);
        if (optional.isEmpty()) {
            // Handle boolean properties.
            optional = property.parse(
                    value.equals("1") ? "true" : "false");
        }

        return optional
                .map(t -> current.with(property, t))
                .orElse(null);
    }

    /**
     * Converts a block state into a block definition.
     *
     * @param state The block state.
     * @return The block definition.
     */
    public static BlockDefinition translate(BlockState state) {
        var id = java2Runtime.get(state);
        return id == null ?
                GameConstants.BLOCKS.get().getDefinition(0) :
                GameConstants.BLOCKS.get().getDefinition(id);
    }
}
