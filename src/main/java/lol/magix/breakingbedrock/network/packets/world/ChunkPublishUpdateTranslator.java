package lol.magix.breakingbedrock.network.packets.world;

import org.cloudburstmc.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;

@Translate(PacketType.BEDROCK)
public final class ChunkPublishUpdateTranslator extends Translator<NetworkChunkPublisherUpdatePacket> {
    @Override
    public Class<NetworkChunkPublisherUpdatePacket> getPacketClass() {
        return NetworkChunkPublisherUpdatePacket.class;
    }

    @Override
    public void translate(NetworkChunkPublisherUpdatePacket packet) {
        var centerPacket = new ChunkRenderDistanceCenterS2CPacket(
                packet.getPosition().getX() >> 4, packet.getPosition().getZ() >> 4);
        this.javaClient().processPacket(centerPacket);
    }
}
