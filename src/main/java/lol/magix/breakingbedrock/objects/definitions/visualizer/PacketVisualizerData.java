package lol.magix.breakingbedrock.objects.definitions.visualizer;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerMessage.PacketIds;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lombok.Builder;

@Builder
public final class PacketVisualizerData {
    @Builder.Default private long time = System.currentTimeMillis();
    @Builder.Default private String source = "client";
    @Builder.Default private int packetId = -1;
    @Builder.Default private String packetName = "undefined";
    @Builder.Default private long length = -1;
    @Builder.Default private String data = ""; // Should be JSON-encoded.

    @Override
    public String toString() {
        return EncodingUtils.jsonEncode(this);
    }

    /**
     * Converts a {@link BedrockPacket} to a {@link PacketVisualizerData} instance.
     * @param packet The packet to convert.
     * @param isOutbound Is the packet going outbound?
     * @return A {@link PacketVisualizerData} instance.
     */
    public static PacketVisualizerMessage toMessage(BedrockPacket packet, boolean isOutbound) {
        var encoded = EncodingUtils.jsonEncode(packet);
        var packetData = PacketVisualizerData.builder()
                .source(isOutbound ? "client" : "server").packetId(packet.getPacketId())
                .packetName(packet.getClass().getSimpleName())
                .length(encoded.length()).data(encoded).build();
        return PacketVisualizerMessage.builder()
                .packetId(PacketIds.PACKET)
                .data(packetData.toString()).build();
    }
}
