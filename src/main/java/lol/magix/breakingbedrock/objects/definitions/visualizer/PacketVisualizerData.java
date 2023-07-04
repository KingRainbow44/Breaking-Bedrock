package lol.magix.breakingbedrock.objects.definitions.visualizer;

import com.google.gson.JsonObject;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerMessage.PacketIds;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lombok.Builder;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

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
        String encoded = "";
        try {
            encoded = EncodingUtils.jsonEncode(packet);
            if (encoded.length() > 10000 && !isOutbound) {
                var truncated = new TruncatedPacketData(encoded.substring(0, 10000));
                encoded = EncodingUtils.jsonEncode(truncated);
            }
        } catch (Exception exception) {
            var object = new JsonObject();
            object.addProperty("error", "Unable to encode packet.");
            object.addProperty("message", exception.getMessage());
        }

        var packetData = PacketVisualizerData.builder()
                .source(isOutbound ? "client" : "server")
                .packetId(-1) // Packet IDs (as numbers) do not exist.
                .packetName(packet.getClass().getSimpleName())
                .length(encoded.length()).data(encoded).build();
        return PacketVisualizerMessage.builder()
                .packetId(PacketIds.PACKET)
                .data(packetData.toString()).build();
    }
}
