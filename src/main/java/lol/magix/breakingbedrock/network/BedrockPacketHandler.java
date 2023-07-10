package lol.magix.breakingbedrock.network;

import lol.magix.breakingbedrock.network.translation.PacketTranslator;
import lol.magix.breakingbedrock.objects.absolute.PacketVisualizer;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerData;
import lombok.Data;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
public final class BedrockPacketHandler
        implements org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler {
    private final BedrockNetworkClient client;

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        var session = BedrockNetworkClient.getHandle();

        if (session.isLogging()) {
            // Visualize inbound packet.
            PacketVisualizer.getInstance().sendMessage(
                    PacketVisualizerData.toMessage(packet, false));
        }

        // Handle the packet.
        return PacketTranslator
                .getBedrockTranslator()
                .translatePacket(packet);
    }

    @Override
    public void onDisconnect(String reason) {
        this.getClient().disconnect(reason);
    }
}
