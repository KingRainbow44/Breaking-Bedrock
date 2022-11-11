package lol.magix.breakingbedrock.network.packets.world;

import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;

@Translate(PacketType.BEDROCK)
public final class ChunkRadiusTranslator extends Translator<ChunkRadiusUpdatedPacket> {
    @Override
    public Class<ChunkRadiusUpdatedPacket> getPacketClass() {
        return ChunkRadiusUpdatedPacket.class;
    }

    @Override
    public void translate(ChunkRadiusUpdatedPacket packet) {
        this.javaClient().processPacket(new ChunkLoadDistanceS2CPacket(packet.getRadius()));
    }
}
