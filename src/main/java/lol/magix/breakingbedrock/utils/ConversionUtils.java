package lol.magix.breakingbedrock.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.GameType;

import java.util.List;

/**
 * Code which converts format A -> format B.
 */
public interface ConversionUtils {
    /**
     * Converts a Bedrock {@link GameType} to a Java {@link GameMode}.
     * @param type The Bedrock game type.
     * @return The Java game mode.
     */
    static GameMode convertBedrockGameMode(GameType type) {
        return switch (type) {
            case SURVIVAL, DEFAULT, SURVIVAL_VIEWER -> GameMode.SURVIVAL;
            case CREATIVE, CREATIVE_VIEWER -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
        };
    }

    /**
     * Converts a Bedrock dimension ID to a Java dimension type.
     * @param dimensionId The Bedrock dimension ID.
     * @return The Java dimension type.
     */
    static RegistryKey<DimensionType> convertBedrockDimension(int dimensionId) {
        return switch (dimensionId) {
            default -> DimensionTypes.OVERWORLD;
            case 1 -> DimensionTypes.THE_NETHER;
            case 2 -> DimensionTypes.THE_END;
        };
    }

    /**
     * Converts a Bedrock dimension ID to a Java {@link RegistryKey}.
     * @param dimensionId The Bedrock dimension ID.
     * @return The Java registry key.
     */
    static RegistryKey<World> convertBedrockWorld(int dimensionId) {
        return switch (dimensionId) {
            default -> World.OVERWORLD;
            case 1 -> World.NETHER;
            case 2 -> World.END;
        };
    }

    /**
     * Updates the server's gamerules on the client.
     * @param gamerules The server gamerules to update.
     */
    static void updateGameRules(List<GameRuleData<?>> gamerules) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        var world = client.world;

        // Check if the client world and player is valid.
        if (world == null || player == null) return;

        // Update gamerules.
        for (var gamerule : gamerules) {
            var value = gamerule.getValue();
            switch (gamerule.getName()) {
                case "dodaylightcycle" -> world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set((Boolean) value, null);
                case "doimmediaterespawn" -> player.setShowsDeathScreen(!((Boolean) value));
            }
        }
    }
}
