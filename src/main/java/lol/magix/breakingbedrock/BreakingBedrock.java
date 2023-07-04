package lol.magix.breakingbedrock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lol.magix.breakingbedrock.network.auth.XboxV2;
import lol.magix.breakingbedrock.network.translation.PacketTranslator;
import lol.magix.breakingbedrock.objects.ThreadFactoryBuilder;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.absolute.PacketVisualizer;
import lol.magix.breakingbedrock.translators.BlockPaletteTranslator;
import lol.magix.breakingbedrock.translators.BlockStateTranslator;
import lol.magix.breakingbedrock.translators.LegacyBlockPaletteTranslator;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class BreakingBedrock {
    @Getter private static final Gson gson
            = new GsonBuilder().create();
    @Getter private static final Logger logger
            = LoggerFactory.getLogger("BreakingBedrock");
    @Getter private static final Reflections reflector
            = new Reflections("lol.magix.breakingbedrock");
    @Getter private static final boolean debugEnabled
            = Objects.equals(System.getProperty("bedrockDebug"), "true");
    @Getter private static final EventLoopGroup eventGroup
            = new NioEventLoopGroup(0, ThreadFactoryBuilder.base());

    /**
     * Initializes high-level mod systems.
     */
    public static void initialize() {
        // Initialize separate systems.
        PacketTranslator.initialize();
        PacketVisualizer.initialize();
        // Load resources.
        GameConstants.loadRegistry();

        BlockPaletteTranslator.loadMappings();
        BlockStateTranslator.loadMappings();
        LegacyBlockPaletteTranslator.loadMappings();

        // Check for an Xbox access token.
        var accessToken = System.getProperty("XboxAccessToken");
        if (accessToken == null || accessToken.isEmpty()) {
            System.setProperty("XboxAccessToken", XboxV2.getAccessToken()); // Attempt to get an access token.
            logger.info("Xbox access token set. {}", System.getProperty("XboxAccessToken"));
        } else {
            logger.info("Xbox access token found. Xbox authentication is enabled.");
        }

        logger.info("Initialized!");
    }

    /**
     * Fetches the username for the current session.
     * @return A username as a String.
     */
    public static String getUsername() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }
}
