package lol.magix.breakingbedrock.network.translation;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.utils.ReflectionUtils;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles packet translation.
 */
public final class PacketTranslator {
    @Getter private static PacketTranslator instance;

    /**
     * Initializes the packet translator.
     */
    public static void initialize() {
        instance = new PacketTranslator();
    }

    private final Map<Class<? extends BedrockPacket>,
            Translator<? extends BedrockPacket>> translators
            = new ConcurrentHashMap<>();

    private PacketTranslator() {
        // Register all packet translators.
        var classes = ReflectionUtils.getAnnotatedWith(Translate.class);
        for (var translator : classes) try {
            var instance = translator.getDeclaredConstructor().newInstance();
            if (instance instanceof Translator<? extends BedrockPacket> translatorInstance)
                this.translators.put(translatorInstance.getPacketClass(), translatorInstance);
        } catch (ReflectiveOperationException ignored) {
            BreakingBedrock.getLogger().warn("Unable to register packet translator: {}", translator.getName());
        }

        BreakingBedrock.getLogger().info("Registered {} packet translators.", this.translators.size());
    }

    /**
     * Translates the specified packet.
     * @param inboundPacket The packet to translate.
     */
    @SuppressWarnings("unchecked")
    public <P extends BedrockPacket> void translatePacket(P inboundPacket) {
        var translator = (Translator<P>) this.translators.get(inboundPacket.getClass());
        if (translator != null) translator.translate(inboundPacket);
    }
}