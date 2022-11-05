package lol.magix.breakingbedrock.objects.definitions.visualizer;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public final class PacketVisualizerMessage {
    private int packetId;
    private String data;

    public static class PacketIds {
        public static final int HANDSHAKE = 0;
        public static final int PACKET = 1;
    }
}