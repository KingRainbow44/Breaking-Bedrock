package lol.magix.breakingbedrock.network.packets.bedrock;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;

@Translate(PacketType.BEDROCK)
public final class DisconnectTranslator extends Translator<DisconnectPacket> {
    @Override
    public Class<DisconnectPacket> getPacketClass() {
        return DisconnectPacket.class;
    }

    @Override
    public void translate(DisconnectPacket packet) {
        this.bedrockClient.disconnect(packet.getKickMessage());
    }
}
