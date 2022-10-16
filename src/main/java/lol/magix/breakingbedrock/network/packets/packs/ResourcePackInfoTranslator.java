package lol.magix.breakingbedrock.network.packets.packs;

import com.nukkitx.protocol.bedrock.packet.ClientCacheStatusPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket.Status;
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;

@Translate(PacketType.BEDROCK)
public final class ResourcePackInfoTranslator extends Translator<ResourcePacksInfoPacket> {
    @Override
    public Class<ResourcePacksInfoPacket> getPacketClass() {
        return ResourcePacksInfoPacket.class;
    }

    @Override
    public void translate(ResourcePacksInfoPacket packet) {
        this.bedrockClient.sendPacket(new ClientCacheStatusPacket(), true);

        // Create resource pack response.
        // TODO: Download & convert server resource packs.
        var response = new ResourcePackClientResponsePacket();
        response.setStatus(Status.HAVE_ALL_PACKS);
        // Send resource pack response.
        this.bedrockClient.sendPacket(response, true);
    }
}
