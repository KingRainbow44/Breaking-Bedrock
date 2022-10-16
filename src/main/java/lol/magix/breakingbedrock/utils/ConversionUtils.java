package lol.magix.breakingbedrock.utils;

import com.nukkitx.protocol.bedrock.data.GameType;
import net.minecraft.world.GameMode;

/**
 * Code which converts format A -> format B.
 */
public interface ConversionsUtils {
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
}
