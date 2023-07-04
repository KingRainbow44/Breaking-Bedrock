package lol.magix.breakingbedrock.network.packets.java.movement;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;

@Translate(PacketType.JAVA)
public final class ClientCommandC2STranslator extends Translator<ClientCommandC2SPacket> {
    @Override
    public Class<ClientCommandC2SPacket> getPacketClass() {
        return ClientCommandC2SPacket.class;
    }

    @Override
    public void translate(ClientCommandC2SPacket packet) {
        var player = this.player();
        if (player == null) return;

        var actionType = switch (packet.getMode()) {
            case PRESS_SHIFT_KEY -> PlayerActionType.START_SNEAK;
            case RELEASE_SHIFT_KEY -> PlayerActionType.STOP_SNEAK;
            case START_SPRINTING -> PlayerActionType.START_SPRINT;
            case STOP_SPRINTING -> PlayerActionType.STOP_SPRINT;
            case STOP_SLEEPING, START_RIDING_JUMP, STOP_RIDING_JUMP,
                    OPEN_INVENTORY, START_FALL_FLYING -> null;
        };

        if (this.data().getMovementMode() != AuthoritativeMovementMode.CLIENT) {
            PlayerAuthInputData inputData = null;
            switch (packet.getMode()) {
                case PRESS_SHIFT_KEY -> inputData = PlayerAuthInputData.START_SNEAKING;
                case RELEASE_SHIFT_KEY -> inputData = PlayerAuthInputData.STOP_SNEAKING;
                case START_SPRINTING -> inputData = PlayerAuthInputData.START_SPRINTING;
                case STOP_SPRINTING -> inputData = PlayerAuthInputData.STOP_SPRINTING;
            }

            if (inputData != null) this.bedrockClient.addInputData(inputData);
        }

        if (actionType == null) return;

        var actionPacket = new PlayerActionPacket();
        actionPacket.setRuntimeEntityId(this.data().getRuntimeId());
        actionPacket.setAction(actionType);
        actionPacket.setBlockPosition(Vector3i.ZERO);
        actionPacket.setResultPosition(Vector3i.ZERO);

        this.bedrockClient.sendPacket(actionPacket);
    }
}
