package lol.magix.breakingbedrock.network.packets.java.movement;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

import java.util.HashMap;
import java.util.Map;

@Translate(PacketType.JAVA)
public final class TeleportConfirmC2STranslator extends Translator<TeleportConfirmC2SPacket> {
    public static final Map<Integer, MovePlayerPacket> TELEPORTS = new HashMap<>();

    @Override
    public Class<TeleportConfirmC2SPacket> getPacketClass() {
        return TeleportConfirmC2SPacket.class;
    }

    @Override
    public void translate(TeleportConfirmC2SPacket packet) {
        // Resolve the teleport.
        var teleport = TELEPORTS.remove(packet.getTeleportId());
        if (teleport == null) return;

        var position = teleport.getPosition();
        var rotation = teleport.getRotation();

        var x = position.getX();
        var y = position.getY();
        var z = position.getZ();

        var yaw = rotation.getY();
        var pitch = rotation.getX();

        // Send the move acknowledgement.
        PlayerMoveC2STranslator.translate(new PlayerMoveC2SPacket.Full(
                x, y, z, yaw, pitch, teleport.isOnGround()
        ), MovePlayerPacket.Mode.TELEPORT);
    }
}
