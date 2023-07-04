package lol.magix.breakingbedrock.network.packets.java.movement;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

@Translate(PacketType.JAVA)
public final class FullMoveC2STranslator extends Translator<Full> {
    @Override
    public Class<Full> getPacketClass() {
        return Full.class;
    }

    @Override
    public void translate(Full packet) {
        PlayerMoveC2STranslator.translate(packet, MovePlayerPacket.Mode.NORMAL);
    }
}
