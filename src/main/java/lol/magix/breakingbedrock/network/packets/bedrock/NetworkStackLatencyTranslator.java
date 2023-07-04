package lol.magix.breakingbedrock.network.packets.bedrock.misc;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;

@Translate(PacketType.BEDROCK)
public final class NetworkStackLatencyTranslator extends Translator<NetworkStackLatencyPacket> {
    @Override
    public Class<NetworkStackLatencyPacket> getPacketClass() {
        return NetworkStackLatencyPacket.class;
    }

    @Override
    public void translate(NetworkStackLatencyPacket packet) {
        if (!packet.isFromServer()) return;

        var response = new NetworkStackLatencyPacket();
        packet.setTimestamp(System.currentTimeMillis());
        this.bedrockClient.sendPacket(response, true);
    }
}
