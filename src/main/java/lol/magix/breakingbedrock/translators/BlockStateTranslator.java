package lol.magix.breakingbedrock.translators;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.Resources;
import lol.magix.breakingbedrock.objects.game.BedrockBlockState;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds mappings from Java Edition block states to Bedrock Edition block states.
 */
public final class BlockStateTranslator {
    private static final Map<String, BlockState> bedrockJavaMappings = new HashMap<>();

    /**
     * Attempts to find the matching block state.
     * @param identifier The identifier of the block.
     * @return The block state, or null if not found.
     */
    public static BlockState getBlockState(String identifier) {
        return bedrockJavaMappings.get(identifier);
    }

    /**
     * Loads mappings from the mappings file.
     */
    public static void loadMappings() {
        var mappings = ResourceUtils.getResourceAsObject(
                Resources.BLOCKS_MAPPINGS, JsonObject.class);
        if (mappings == null) return;

        for (var entry : mappings.entrySet()) {
            var javaState = entry.getKey();
            var bedrockState = entry.getValue().getAsJsonObject();

            // Create a Bedrock block state mapping.
            var blockState = new BedrockBlockState();
            blockState.setIdentifier(bedrockState.get("bedrock_identifier").getAsString());

            // Check if the entry has additional states.
            if (bedrockState.has("bedrock_states")) {
                for (var stateEntry : bedrockState.get("bedrock_states").getAsJsonObject().entrySet()) {
                    if (!(stateEntry.getValue() instanceof JsonPrimitive jsonValue)) continue;

                    String value; if (jsonValue.isString())
                        value = jsonValue.getAsString();
                    else if (jsonValue.isNumber())
                        value = String.valueOf(jsonValue.getAsNumber());
                    else if (jsonValue.isBoolean())
                        value = String.valueOf(jsonValue.getAsBoolean());
                    else {
                        BreakingBedrock.getLogger().warn("Invalid block state value.");
                        continue;
                    }

                    blockState.getProperties().put(stateEntry.getKey(), value);
                }
            }

            bedrockJavaMappings.put(blockState.toString(),
                    BlockStateTranslator.parseBlockState(javaState));
        }

        BreakingBedrock.getLogger().info("Loaded {} block state mappings.", bedrockJavaMappings.size());
    }

    /**
     * Parses Java block state data from a string.
     * @param state The string to parse.
     * @return The parsed block state.
     */
    public static BlockState parseBlockState(String state) {
        // Get the block state identifier.
        var identifierEnd = state.indexOf('[');
        var identifier = identifierEnd == -1 ? state :
                state.substring(0, identifierEnd);

        // Get a block from the identifier.
        var block = Registry.BLOCK.get(new Identifier(identifier));
        if (block == Blocks.AIR && !identifier.equals("minecraft:air")) {
            BreakingBedrock.getLogger().warn("Unknown block identifier: " + identifier);
            return null;
        }

        // Find the correct block state.
        var blockState = block.getDefaultState();
        if (identifierEnd != -1) {
            var properties = state.substring(identifierEnd + 1, state.length() - 1);
            for (var property : properties.split(",")) {
                // Get property data.
                var propertySplit = property.split("=");
                var propertyKey = propertySplit[0];
                var propertyValue = propertySplit[1];

                // Find the property object.
                var propertyObject = block.getStateManager().getProperty(propertyKey);
                if (propertyObject == null) {
                    BreakingBedrock.getLogger().warn("Unknown block property: " + propertyKey);
                    continue;
                }

                blockState = BlockStateTranslator.parsePropertyValue(blockState, propertyObject, propertyValue);
                if (blockState == null) {
                    BreakingBedrock.getLogger().warn("Unknown block property value: " + propertyValue);
                    return null;
                }
            }
        }

        return blockState;
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
        return optional.map(t -> current.with(property, t)).orElse(null);
    }
}
