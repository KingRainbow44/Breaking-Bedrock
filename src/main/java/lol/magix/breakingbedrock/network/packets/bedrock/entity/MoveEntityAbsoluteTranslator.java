package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.entity.player.PlayerEntity;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket;

@Translate(PacketType.BEDROCK)
public final class MoveEntityAbsoluteTranslator extends Translator<MoveEntityAbsolutePacket> {
    @Override
    public Class<MoveEntityAbsolutePacket> getPacketClass() {
        return MoveEntityAbsolutePacket.class;
    }

    @Override
    public void translate(MoveEntityAbsolutePacket packet) {
        var world = this.client().world;
        if (world == null) return;

        // Get the entity.
        var runtimeId = (int) packet.getRuntimeEntityId();
        var entity = world.getEntityById(runtimeId);
        if (entity == null) return;

        // Get the position.
        var position = packet.getPosition();
        var x = position.getX();
        var y = position.getY();
        var z = position.getZ();

        // Adjust the Y level by offsetting the player height.
        if (entity instanceof PlayerEntity)
            y -= GameConstants.PLAYER_OFFSET;

        var rotation = packet.getRotation();
        var pitch = rotation.getX();
        var yaw = rotation.getY();
        var headYaw = rotation.getZ();

        // Update the entity.
        entity.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, 3, true);
        entity.updateTrackedHeadRotation(headYaw, 3);
        entity.setOnGround(packet.isOnGround());
    }
}
