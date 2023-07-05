package lol.magix.breakingbedrock.network.packets.bedrock;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.TextUtils;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;

@Translate(PacketType.BEDROCK)
public final class TextTranslator extends Translator<TextPacket> {
    @Override
    public Class<TextPacket> getPacketClass() {
        return TextPacket.class;
    }

    @Override
    public void translate(TextPacket packet) {
        switch (packet.getType()) {
            default -> this.logger.warn("Unknown text packet type: {}", packet.getType());
            case RAW -> {
                var messagePacket = new GameMessageS2CPacket(
                        Text.of(packet.getMessage()), false);
                this.javaClient().processPacket(messagePacket);
            }
            case CHAT -> {
                var formatted = "<" + packet.getSourceName() + "> " + packet.getMessage();
                var messagePacket = new GameMessageS2CPacket(
                        Text.of(formatted), false);
                this.javaClient().processPacket(messagePacket);
            }
            case TRANSLATION -> {
                // Parse translation data.
                var translationKey = TextUtils.strip(packet.getMessage()
                        .replaceAll("%", ""));
                var parameters = packet.getParameters().toArray();
                // Check if the translation key is valid.
                if (!TranslationStorage.getInstance().hasTranslation(translationKey)) {
                    this.logger.warn("Unknown translation key: {}", translationKey);
                } else {
                    var textContainer = TextUtils.translate(packet.getMessage(), parameters);
                    var messagePacket = new GameMessageS2CPacket(textContainer, false);
                    this.javaClient().processPacket(messagePacket);
                }
            }
        }
    }
}
