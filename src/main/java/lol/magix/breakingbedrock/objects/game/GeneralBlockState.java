package lol.magix.breakingbedrock.objects.game;

import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.cloudburstmc.nbt.NbtMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a general block state.
 */
@Data
@EqualsAndHashCode
public final class GeneralBlockState {
    private String namespace = "minecraft";
    private String identifier;
    private final Map<String, String> properties = new HashMap<>();

    public GeneralBlockState() {
        // Empty constructor.
        // Fields should be set later.
    }

    public GeneralBlockState(String data) {
        this.namespace = "minecraft";
        var firstColonIndex = data.indexOf(":");
        if (firstColonIndex != -1) {
            this.namespace = data.substring(0, firstColonIndex++);
        } else {
            firstColonIndex = 0;
        }

        var firstLeftBracketIndex = data.indexOf("[");
        if (firstLeftBracketIndex != -1) { //if its found
            this.identifier = data.substring(firstColonIndex, firstLeftBracketIndex++);
        } else {
            this.identifier = data.substring(firstColonIndex);
        }

        if (firstLeftBracketIndex != -1) {
            var blockProperties = data.substring(firstLeftBracketIndex, data.length() - 1);
            if (!blockProperties.isEmpty()) {
                var blockPropertyKeysAndValues = blockProperties.split(",");
                for (var keyAndValue : blockPropertyKeysAndValues) {
                    var keyAndValueArray = keyAndValue.split("=");

                    var value = keyAndValueArray[1];
                    this.properties.put(keyAndValueArray[0], switch (value) {
                        default -> value;
                        case "true" -> "1";
                        case "false" -> "0";
                    });
                }
            }

            // Sort the properties by property name length.
            var sortedProperties = new HashMap<String, String>();
            this.properties.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey((o1, o2) -> Integer.compare(o2.length(), o1.length())))
                    .forEachOrdered(x -> sortedProperties.put(x.getKey(), x.getValue()));

            this.properties.clear();
            this.properties.putAll(sortedProperties);
        }
    }

    public GeneralBlockState(NbtMap map) {
        var blockName = map.getString("name");
        var blockStates = map.getCompound("states");

        this.namespace = "minecraft";
        var firstColonIndex = blockName.indexOf(":");
        if (firstColonIndex != -1) {
            this.namespace = blockName.substring(0, firstColonIndex++);
        } else {
            firstColonIndex = 0;
        }

        this.identifier = blockName.substring(firstColonIndex);
        for (var blockState : blockStates.entrySet()) {
            this.properties.put(
                    blockState.getKey(),
                    blockState.getValue().toString()
            );
        }
    }

    /**
     * @return The block as a Minecraft Java block.
     */
    @Nullable
    public Block toJavaBlock() {
        var block = Registries.BLOCK.get(new Identifier(
                this.getNamespace(), this.getIdentifier()));
        return block == Blocks.AIR &&
                !this.getIdentifier().equals("air")
                ? null : block;
    }

    /**
     * @return The block state as a Minecraft Java block state.
     */
    @Nullable
    public BlockState toJavaBlockState() {
        var block = this.toJavaBlock();
        if (block == null) return null;

        var state = block.getDefaultState();
        for (var property : this.properties.entrySet()) {
            // Find the property object.
            var propertyObject = block.getStateManager().getProperty(property.getKey());
            if (propertyObject == null) {
                BreakingBedrock.getLogger().warn("Unknown block property: " + property.getKey());
                continue;
            }

            state = BlockStateTranslator.parsePropertyValue(state, propertyObject, property.getValue());
            if (state == null) {
                BreakingBedrock.getLogger().warn("Unknown block property value: " + property.getValue());
                return null;
            }
        }

        return state;
    }

    @Override
    public String toString() {
        if (this.identifier == null)
            return null;

        var hasProperties = this.properties.size() > 0;
        var builder = new StringBuilder(
                this.namespace + ":" + this.identifier);

        if (hasProperties) builder.append("[");
        for (var entry : this.properties.entrySet()) {
            builder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append(",");
        }
        if (hasProperties) builder.deleteCharAt(builder.length() - 1);

        return builder + (hasProperties ? "]" : "");
    }
}
