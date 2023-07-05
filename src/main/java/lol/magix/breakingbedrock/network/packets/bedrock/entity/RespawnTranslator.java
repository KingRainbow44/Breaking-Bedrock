package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket;

import java.util.Optional;

@Translate(PacketType.BEDROCK)
public final class RespawnTranslator extends Translator<RespawnPacket> {
    @Override
    public Class<RespawnPacket> getPacketClass() {
        return RespawnPacket.class;
    }

    @Override
    public void translate(RespawnPacket packet) {
        var player = this.player();
        if (player == null) return;

        var interactionManager = this.client().interactionManager;
        if (interactionManager == null) return;

        if (packet.getState() == RespawnPacket.State.SERVER_READY) {
            // Respawn the player on the server.
            var actionPacket = new PlayerActionPacket();
            actionPacket.setRuntimeEntityId(this.data().getRuntimeId());
            actionPacket.setAction(PlayerActionType.RESPAWN);
            actionPacket.setBlockPosition(Vector3i.ZERO);
            actionPacket.setResultPosition(Vector3i.ZERO);
            actionPacket.setFace(-1);
            this.bedrockClient.sendPacket(actionPacket);

            // Change the dimension on the client.
            var gameMode = interactionManager.getCurrentGameMode();
            this.javaClient().processPacket(new PlayerRespawnS2CPacket(
                    DimensionTypes.OVERWORLD, World.OVERWORLD, -1, gameMode, gameMode,
                    false, false, (byte) 0, Optional.empty(), 0
            ));
        }
    }
}
