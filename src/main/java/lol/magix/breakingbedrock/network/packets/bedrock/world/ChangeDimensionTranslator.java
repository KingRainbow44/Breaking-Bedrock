package lol.magix.breakingbedrock.network.packets.bedrock.world;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;

import java.util.Optional;

@Translate(PacketType.BEDROCK)
public final class ChangeDimensionTranslator extends Translator<ChangeDimensionPacket> {
    @Override
    public Class<ChangeDimensionPacket> getPacketClass() {
        return ChangeDimensionPacket.class;
    }

    @Override
    public void translate(ChangeDimensionPacket packet) {
        var interactionManager = this.client().interactionManager;
        if (interactionManager == null) return;

        this.javaClient().processPacket(new PlayerRespawnS2CPacket(
                ConversionUtils.convertBedrockDimension(packet.getDimension()),
                ConversionUtils.convertBedrockWorld(packet.getDimension()), 0L,
                interactionManager.getCurrentGameMode(),
                interactionManager.getPreviousGameMode(),
                false, false, (byte) 0, Optional.empty(), 0
        ));

        var position = packet.getPosition();
        var player = this.player();
        if (player != null) {
            player.setPosition(GameUtils.convert(position));
        }

        // Set the spawn position.
        this.javaClient().processPacket(new PlayerSpawnPositionS2CPacket(
                GameUtils.toBlockPos(packet.getPosition().toInt()), 0f
        ));

        // Finish the dimension.
        var actionPacket = new PlayerActionPacket();
        actionPacket.setRuntimeEntityId(this.data().getRuntimeId());
        actionPacket.setAction(PlayerActionType.DIMENSION_CHANGE_SUCCESS);
        actionPacket.setBlockPosition(position.toInt());
        actionPacket.setResultPosition(position.toInt());
        actionPacket.setFace(0);

        this.bedrockClient.sendPacket(actionPacket);
    }
}
