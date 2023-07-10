package lol.magix.breakingbedrock.objects.game;

import lol.magix.breakingbedrock.events.defaults.PlayerTickEvent;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

import java.util.HashSet;
import java.util.Set;

@Data
public final class AuthInputHandler {
    @Getter private final Set<PlayerAuthInputData> inputData
            = new HashSet<>();
    private final BedrockNetworkClient client;

    @Setter private PlayerAuthInputPacket nextPacket = null;

    public AuthInputHandler(BedrockNetworkClient client) {
        this.client = client;

        client.getEventManager().registerListener(
                PlayerTickEvent.class, this::onPlayerTick);
    }

    /**
     * Invoked when the client ticks.
     *
     * @param event The event.
     */
    private void onPlayerTick(PlayerTickEvent event) {
        var client = this.getClient();
        var data = client.getData();

        // If the packet has not been set, return.
        var packet = this.nextPacket;
        if (packet == null) return;

        // Set the packet's data.
        packet.setTick(event.getTick());
        packet.getInputData().clear();
        packet.getPlayerActions().clear();
        if (data.isJumping()) {
            packet.getInputData().add(PlayerAuthInputData.START_JUMPING);
        }
        if (this.getInputData().contains(PlayerAuthInputData.PERFORM_BLOCK_ACTIONS)) {
            if (packet.getPlayerActions().addAll(client.getBlockActions())) {
                client.getBlockActions().clear();
            }
        }
        if (packet.getInputData().addAll(this.inputData)) {
            this.getInputData().clear();
        }

        // Send the packet.
        client.sendPacket(packet);
    }
}
