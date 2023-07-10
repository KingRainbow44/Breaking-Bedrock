package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.packets.java.movement.PlayerMoveC2STranslator;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

@Translate(PacketType.BEDROCK)
public final class MovePlayerTranslator extends Translator<MovePlayerPacket> {
    private final AtomicInteger teleportId = new AtomicInteger(2);

    @Override
    public Class<MovePlayerPacket> getPacketClass() {
        return MovePlayerPacket.class;
    }

    @Override
    public void translate(MovePlayerPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var runtimeId = (int) packet.getRuntimeEntityId();
        var position = packet.getPosition();
        var rotation = packet.getRotation();

        var x = position.getX();
        var y = position.getY();
        var z = position.getZ();

        var pitch = rotation.getX();
        var yaw = rotation.getY();
        var headYaw = rotation.getZ();

        this.run(() -> {
            // Check if the packet was targeted at us.
            if (this.player().getId() == runtimeId) {
                // Update the player's position.
                this.javaClient().processPacket(new PlayerPositionLookS2CPacket(
                        x, y, z, yaw, pitch, Collections.emptySet(), this.teleportId.getAndIncrement()));
                // Send the move acknowledgement.
                PlayerMoveC2STranslator.translate(new PlayerMoveC2SPacket.Full(
                        x, y, z, yaw, pitch, packet.isOnGround()
                ), MovePlayerPacket.Mode.TELEPORT);
                return;
            }

            // Fetch the entity which is being referenced.
            var entity = world.getEntityById(runtimeId);
            if (entity == null) return;

            // Update the entity's position.
            entity.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, 3, true);
            entity.updateTrackedHeadRotation(headYaw, 3);
            entity.setOnGround(packet.isOnGround());
        });
    }
}
