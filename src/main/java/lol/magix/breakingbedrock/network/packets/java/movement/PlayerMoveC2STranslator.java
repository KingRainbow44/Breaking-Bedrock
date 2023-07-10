package lol.magix.breakingbedrock.network.packets.java.movement;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.ClientPlayMode;
import org.cloudburstmc.protocol.bedrock.data.InputInteractionModel;
import org.cloudburstmc.protocol.bedrock.data.InputMode;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

@Translate(PacketType.JAVA)
public final class PlayerMoveC2STranslator extends Translator<PlayerMoveC2SPacket> {
    private static Vector3f lastPosition = Vector3f.ZERO;
    private static Vector3f lastRotation = Vector3f.ZERO;
    private static boolean lastOnGround = false;

    public static void translate(
            PlayerMoveC2SPacket packet,
            MovePlayerPacket.Mode moveMode
    ) {
        var bedrockClient = BedrockNetworkClient.getInstance();
        var data = bedrockClient.getData();

        // Check if the player is ready.
        if (!bedrockClient.checkReadyState()) return;

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        var onGround = packet.isOnGround();
        var runtimeId = data.getRuntimeId();

        // Calculate the player's position.
        var currentX = packet.getX(player.getPos().getX());
        var currentY = packet.getY(player.getPos().getY()) +
                player.getEyeHeight(EntityPose.STANDING);
        var currentZ = packet.getZ(player.getPos().getZ());
        var currentPos = Vector3f.from(currentX, currentY, currentZ);

        // Calculate the player's rotation.
        var currentPitch = packet.getPitch(player.getPitch());
        var currentYaw = packet.getYaw(player.getYaw());
        var currentHeadYaw = player.getHeadYaw();
        var currentRot = Vector3f.from(currentPitch, currentYaw, currentHeadYaw);

        switch (data.getMovementMode()) {
            case CLIENT -> {
                // Compare with last position.
                if (lastPosition.equals(currentPos) && lastRotation.equals(currentRot) &&
                        lastOnGround == onGround) return;

                var movePacket = new MovePlayerPacket();
                movePacket.setRuntimeEntityId(runtimeId);
                movePacket.setPosition(currentPos);
                movePacket.setRotation(currentRot);
                movePacket.setOnGround(onGround);
                movePacket.setMode(moveMode);

                bedrockClient.sendPacket(movePacket);
            }
            case SERVER, SERVER_WITH_REWIND -> {
                var movePacket = new PlayerAuthInputPacket();
                movePacket.setInputMode(InputMode.MOUSE);
                movePacket.setPlayMode(ClientPlayMode.NORMAL);
                movePacket.setInputInteractionModel(InputInteractionModel.CROSSHAIR);
                movePacket.setVrGazeDirection(Vector3f.ZERO);
                movePacket.setDelta(currentPos.sub(PlayerMoveC2STranslator.lastPosition));
                movePacket.setMotion(movePacket.getDelta().toVector2(true));
                movePacket.setPosition(currentPos);
                movePacket.setAnalogMoveVector(Vector2f.ZERO);

                // The yaw is set twice for compatibility reasons.
                movePacket.setRotation(Vector3f.from(
                        currentPitch, currentYaw, currentYaw));
                bedrockClient.getInputHandler().setNextPacket(movePacket);
            }
        }

        // Update last variables.
        PlayerMoveC2STranslator.lastPosition = currentPos;
        PlayerMoveC2STranslator.lastRotation = currentRot;
        PlayerMoveC2STranslator.lastOnGround = onGround;
    }

    @Override
    public Class<PlayerMoveC2SPacket> getPacketClass() {
        return PlayerMoveC2SPacket.class;
    }

    @Override
    public void translate(PlayerMoveC2SPacket packet) {
        PlayerMoveC2STranslator.translate(packet, MovePlayerPacket.Mode.NORMAL);
    }
}
