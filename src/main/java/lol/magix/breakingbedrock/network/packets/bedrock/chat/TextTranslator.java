package lol.magix.breakingbedrock.network.packets.bedrock.chat;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.TextUtils;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket.Type;

@Translate(PacketType.BEDROCK)
public final class TextTranslator extends Translator<TextPacket> {
    @Override
    public Class<TextPacket> getPacketClass() {
        return TextPacket.class;
    }

    @Override
    public void translate(TextPacket packet) {
        var type = packet.getType();
        switch (type) {
            default -> this.logger.warn("Unknown text packet type: {}", type);
            case RAW, TIP -> {
                var messagePacket = new GameMessageS2CPacket(
                        Text.of(packet.getMessage()), type == Type.TIP);
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
                Text textContainer;
                if (!TranslationStorage.getInstance().hasTranslation(translationKey)) {
                    this.logger.warn("Unknown translation key: {}", translationKey);
                    textContainer = TextUtils.translate(packet.getMessage());
                } else {
                    textContainer = TextUtils.translation(packet.getMessage(), parameters);
                }

                var messagePacket = new GameMessageS2CPacket(textContainer, false);
                this.javaClient().processPacket(messagePacket);
            }
        }
    }
}
