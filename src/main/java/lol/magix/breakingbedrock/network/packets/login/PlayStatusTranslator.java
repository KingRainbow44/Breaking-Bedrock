package lol.magix.breakingbedrock.network.packets.login;

import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;

@Translate(PacketType.BEDROCK)
public final class PlayStatusTranslator extends Translator<PlayStatusPacket> {
    @Override
    public Class<PlayStatusPacket> getPacketClass() {
        return PlayStatusPacket.class;
    }

    @Override
    public void translate(PlayStatusPacket packet) {
        var status = packet.getStatus();

        switch (status) {
            default -> this.logger.warn("Unknown play status: " + status);

            case LOGIN_SUCCESS -> {
                if (this.shouldLog)
                    this.logger.info("Login accepted by server.");
            }

            case PLAYER_SPAWN -> {
                if (this.shouldLog)
                    this.logger.info("Attempting to spawn player...");
            }
        }
    }
}
