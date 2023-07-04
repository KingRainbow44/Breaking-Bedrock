package lol.magix.breakingbedrock.network.packets.java.movement;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

@Translate(PacketType.JAVA)
public final class LookAndOnGroundMoveC2STranslator extends Translator<LookAndOnGround> {
    @Override
    public Class<LookAndOnGround> getPacketClass() {
        return LookAndOnGround.class;
    }

    @Override
    public void translate(LookAndOnGround packet) {
        PlayerMoveC2STranslator.translate(packet, MovePlayerPacket.Mode.HEAD_ROTATION);
    }
}
