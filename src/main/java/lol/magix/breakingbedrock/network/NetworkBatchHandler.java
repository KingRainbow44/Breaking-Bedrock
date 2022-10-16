package lol.magix.breakingbedrock.network;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import io.netty.buffer.ByteBuf;
import lol.magix.breakingbedrock.network.translation.PacketTranslator;
import lol.magix.breakingbedrock.objects.PacketVisualizer;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerData;

import java.util.Collection;

/**
 * Handles batch packets.
 */
public final class NetworkBatchHandler implements BatchHandler {
    @Override
    public void handle(BedrockSession session, ByteBuf compressed, Collection<BedrockPacket> packets) {
        for (var packet : packets) {
            if (session.isLogging()) {
                // Visualize inbound packet.
                PacketVisualizer.getInstance().sendMessage(
                        PacketVisualizerData.toMessage(packet, false));
            }

            // Handle the packet.
            PacketTranslator.getBedrockTranslator().translatePacket(packet);
        }
    }
}