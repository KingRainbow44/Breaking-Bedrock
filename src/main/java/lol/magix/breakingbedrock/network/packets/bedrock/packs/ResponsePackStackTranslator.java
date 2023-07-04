package lol.magix.breakingbedrock.network.packets.bedrock.packs;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket.Status;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackStackPacket;

@Translate(PacketType.BEDROCK)
public final class ResponsePackStackTranslator extends Translator<ResourcePackStackPacket> {
    @Override
    public Class<ResourcePackStackPacket> getPacketClass() {
        return ResourcePackStackPacket.class;
    }

    @Override
    public void translate(ResourcePackStackPacket packet) {
        // Create resource pack response.
        var response = new ResourcePackClientResponsePacket();
        response.setStatus(Status.COMPLETED);
        // Send resource pack response.
        this.bedrockClient.sendPacket(response, true);
    }
}
