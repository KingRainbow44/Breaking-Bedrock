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
import lol.magix.breakingbedrock.translators.ItemTranslator;
import lol.magix.breakingbedrock.translators.blockstate.BlockPaletteTranslator;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.translators.entity.EntityTranslator;
import lol.magix.breakingbedrock.translators.blockstate.LegacyBlockPaletteTranslator;
import lol.magix.breakingbedrock.translators.blockentity.BlockEntityRegistry;
import lol.magix.breakingbedrock.translators.pack.ResourcePackTranslator;
import lol.magix.breakingbedrock.translators.screen.ScreenHandlerTranslator;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import okhttp3.OkHttpClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

public final class BreakingBedrock {
    @Getter private static final Gson gson
            = new GsonBuilder().create();
    @Getter private static final Logger logger
            = LoggerFactory.getLogger("BreakingBedrock");
    @Getter private static final Reflections reflector
            = new Reflections("lol.magix.breakingbedrock");
    @Getter private static final OkHttpClient httpClient
            = new OkHttpClient();
    @Getter private static final File dataDirectory
            = new File(MinecraftClient.getInstance().runDirectory,
            "config/breakingbedrock");
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
        ResourcePackTranslator.initialize();
        ScreenHandlerTranslator.initialize();
        EntityMetadataTranslator.initialize();

        // Load resources.
        GameConstants.loadRegistry();
        BlockEntityRegistry.loadRegistry();

        // Load mappings.
        BlockPaletteTranslator.loadMappings();
        BlockStateTranslator.loadMappings();
        LegacyBlockPaletteTranslator.loadMappings();
        EntityTranslator.loadMappings();
        ItemTranslator.loadMappings();

        // Check if the data directory exists.
        if (!dataDirectory.exists()) {
            if (!dataDirectory.mkdirs())
                logger.error("Failed to create data directory.");
            else
                logger.info("Data directory created.");
        }

        // Check for an Xbox access token.
        var accessToken = System.getProperty("XboxAccessToken");
        if (accessToken == null || accessToken.isEmpty()) {
            System.setProperty("XboxAccessToken", XboxV2.getAccessToken()); // Attempt to get an access token.
            logger.info("Xbox access token set. {}", System.getProperty("XboxAccessToken"));
        } else {
            logger.info("Xbox access token found. Xbox authentication is enabled.");
        }

        logger.info("Breaking Bedrock has finished loading.");
    }

    /**
     * Fetches the username for the current session.
     * @return A username as a String.
     */
    public static String getUsername() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }
}
