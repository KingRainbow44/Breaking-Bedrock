package lol.magix.breakingbedrock.network.translation;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.ReflectionUtils;
import lombok.Getter;
import net.minecraft.network.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles packet translation.
 */
public final class PacketTranslator {
    @Getter private static PacketTranslator bedrockTranslator;
    @Getter private static PacketTranslator javaTranslator;

    /**
     * Initializes the packet translator.
     */
    public static void initialize() {
        bedrockTranslator = new PacketTranslator(BedrockPacket.class);
        javaTranslator = new PacketTranslator(Packet.class);

        BreakingBedrock.getLogger().info("Initialized packet translators!");
    }

    private final Map<Class<?>,
            Translator<?>> translators
            = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private PacketTranslator(Class<?> basePacketClass) {
        // Register all packet translators.
        var classes = ReflectionUtils.getAnnotatedWith(Translate.class);
        for (var translator : classes) try {
            // Check the handler's packet type.
            var packetType = translator.getAnnotation(Translate.class).value();
            if (!PacketType.matches(packetType, basePacketClass)) {
                continue;
            }

            // Create an instance of the packet handler.
            var instance = translator.getDeclaredConstructor().newInstance();
            if (instance instanceof Translator translatorInstance)
                this.translators.put(translatorInstance.getPacketClass(), translatorInstance);
        } catch (Exception ignored) {
            BreakingBedrock.getLogger().warn("Unable to register packet translator: {}", translator.getName());
        }

        BreakingBedrock.getLogger().info("Registered {} packet translators.", this.translators.size());
    }

    /**
     * Translates the specified packet.
     * @param inboundPacket The packet to translate.
     */
    @SuppressWarnings("unchecked")
    public <T> void translatePacket(T inboundPacket) {
        var translator = (Translator<T>) this.translators.get(inboundPacket.getClass());
        if (translator != null) translator.translate(inboundPacket);
    }
}