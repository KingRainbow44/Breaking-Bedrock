package lol.magix.breakingbedrock.network.packets.java.movement;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

@Translate(PacketType.JAVA)
public final class PositionAndOnGroundMoveC2STranslator extends Translator<PositionAndOnGround> {
    @Override
    public Class<PositionAndOnGround> getPacketClass() {
        return PositionAndOnGround.class;
    }

    @Override
    public void translate(PositionAndOnGround packet) {
        PlayerMoveC2STranslator.translate(packet, MovePlayerPacket.Mode.NORMAL);
    }
}
