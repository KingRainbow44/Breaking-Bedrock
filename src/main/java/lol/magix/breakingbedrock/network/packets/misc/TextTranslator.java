package lol.magix.breakingbedrock.network.packets.misc;

import com.nukkitx.protocol.bedrock.packet.TextPacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

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
                // TODO: Handle translation messages.
                this.logger.warn("Un-handled translation message: {}", packet.getMessage());
            }
        }
    }
}
