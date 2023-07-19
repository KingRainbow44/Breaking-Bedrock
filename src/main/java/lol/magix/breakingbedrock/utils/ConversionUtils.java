package lol.magix.breakingbedrock.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;

import java.awt.*;
import java.io.ByteArrayInputStream;
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
            case SURVIVAL, SURVIVAL_VIEWER, DEFAULT -> GameMode.SURVIVAL;
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

    /**
     * Converts a Bedrock {@link ContainerSlotType} to a container ID.
     *
     * @param type The Bedrock container slot type.
     * @param fallback The fallback container ID.
     * @return The container ID.
     */
    static int typeToContainer(ContainerSlotType type, int fallback) {
        return switch (type) {
            default -> fallback;
            case INVENTORY, HOTBAR, HOTBAR_AND_INVENTORY -> ContainerId.INVENTORY;
            case OFFHAND -> ContainerId.OFFHAND;
            case ARMOR -> ContainerId.ARMOR;
            case CURSOR -> ContainerId.UI;
        };
    }

    /**
     * Converts a player permission enum into a permission level.
     *
     * @param type The player permission enum.
     * @return The permission level as an integer.
     */
    static int typeToPermission(PlayerPermission type) {
        return switch (type) {
            case VISITOR -> 0;
            case MEMBER -> 1;
            case OPERATOR -> 2;
            case CUSTOM -> 3;
        };
    }

    /**
     * Converts a Bedrock image to a Java native image.
     *
     * @param image The Bedrock image.
     * @return The Java native image.
     */
    static NativeImage imageToNative(ImageData image) {
        var nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        var inputStream = new ByteArrayInputStream(image.getImage());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                var r = inputStream.read();
                var g = inputStream.read();
                var b = inputStream.read();
                var a = inputStream.read();
                nativeImage.setColor(x, y, new Color(r, g, b, a).getRGB());
            }
        }

        return nativeImage;
    }
}
