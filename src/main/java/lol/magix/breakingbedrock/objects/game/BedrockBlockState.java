package lol.magix.breakingbedrock.objects.game;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a Bedrock block state.
 */
public final class BedrockBlockState {
    @Getter @Setter private String identifier;
    @Getter private final Map<String, String> properties = new HashMap<>();

    public BedrockBlockState() {
        // Empty constructor.
        // Fields should be set later.
    }

    public BedrockBlockState(String data) {
        // Get the identifier.
        var identifierEnd = data.indexOf('[');
        this.identifier = identifierEnd == -1 ? data :
                data.substring(0, identifierEnd);

        // Get the properties.
        if (identifierEnd != -1) {
            var properties = data.substring(identifierEnd + 1, data.length() - 1);

            for (var property : properties.split(",")) {
                var split = property.split("=");
                this.properties.put(split[0], split[1]);
            }
        }
    }

    @Override
    public String toString() {
        if (this.identifier == null)
            return null;

        var builder = new StringBuilder(this.identifier + "[");
        for (var entry : this.properties.entrySet()) {
            builder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append(",");
        }

        return builder.substring(0, builder.length() - 1) + "]";
    }
}
