package lol.magix.breakingbedrock.network.packets.java;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestChunkRadiusPacket;

@Translate(PacketType.JAVA)
public final class ClientSettingsC2STranslator extends Translator<ClientSettingsC2SPacket> {
    @Override
    public Class<ClientSettingsC2SPacket> getPacketClass() {
        return ClientSettingsC2SPacket.class;
    }

    @Override
    public void translate(ClientSettingsC2SPacket packet) {
        var data = this.data();

        if (data.getViewDistance() != packet.viewDistance()) {
            var distancePacket = new RequestChunkRadiusPacket();
            distancePacket.setRadius(packet.viewDistance());
            this.bedrockClient.sendPacket(distancePacket);

            data.setViewDistance(packet.viewDistance());
        }
    }
}
