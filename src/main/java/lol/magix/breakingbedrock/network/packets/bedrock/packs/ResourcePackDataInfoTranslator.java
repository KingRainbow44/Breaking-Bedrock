package lol.magix.breakingbedrock.network.packets.bedrock.packs;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.pack.ResourcePackDownloadHandle;
import lol.magix.breakingbedrock.translators.pack.ResourcePackInfo;
import lol.magix.breakingbedrock.translators.pack.ResourcePackTranslator;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackDataInfoPacket;

@Translate(PacketType.BEDROCK)
public final class ResourcePackDataInfoTranslator extends Translator<ResourcePackDataInfoPacket> {
    @Override
    public Class<ResourcePackDataInfoPacket> getPacketClass() {
        return ResourcePackDataInfoPacket.class;
    }

    @Override
    public void translate(ResourcePackDataInfoPacket packet) {
        ResourcePackTranslator.addPack(new ResourcePackDownloadHandle(
                this.bedrockClient,
                new ResourcePackInfo(
                        packet.getPackId(),
                        packet.getPackVersion(),
                        packet.getCompressedPackSize(),
                        null, null, null,
                        false, false
                ),
                packet.getChunkCount()
        ));
    }
}
