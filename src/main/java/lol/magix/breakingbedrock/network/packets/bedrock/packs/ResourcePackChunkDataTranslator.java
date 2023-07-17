package lol.magix.breakingbedrock.network.packets.bedrock.packs;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.pack.ResourcePackTranslator;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackChunkDataPacket;

@Translate(PacketType.BEDROCK)
public final class ResourcePackChunkDataTranslator extends Translator<ResourcePackChunkDataPacket> {
    @Override
    public Class<ResourcePackChunkDataPacket> getPacketClass() {
        return ResourcePackChunkDataPacket.class;
    }

    @Override
    public void translate(ResourcePackChunkDataPacket packet) {
        ResourcePackTranslator.downloadPack(
                packet.getPackId(), packet.getData());
    }
}
