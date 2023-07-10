package lol.magix.breakingbedrock.objects.absolute;

import com.google.gson.Gson;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerMessage;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerMessage.PacketIds;
import lombok.Getter;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Debugger class which logs all packets to a webs
 */
public final class PacketVisualizer extends WebSocketServer {
    @Getter private static PacketVisualizer instance;
    @Getter private static boolean enabled = false;

    static {
        // Enable packet visualizer.
        var property = System.getProperty("VisualizePackets");
        enabled = Boolean.parseBoolean(property);
    }

    /**
     * Initializes the packet visualizer.
     */
    public static void initialize() {
        var port = Integer.parseInt(System.getProperty("VisualizePacketsPort", "8080"));
        instance = new PacketVisualizer(port);

        // Start the server.
        instance.start();
    }

    private final Logger logger = LoggerFactory.getLogger("Packet Visualizer");
    private final Gson gson = BreakingBedrock.getGson();
    private WebSocket relayClient = null;

    /**
     * Creates a new packet visualizer.
     * @param port The port to listen on.
     */
    private PacketVisualizer(int port) {
        super(new InetSocketAddress(port));
    }

    /*
     * Utility methods.
     */

    /**
     * JSON-encodes the provided message.
     * Sends the encoded message to the connected client.
     * @param object The object to encode.
     */
    public void sendMessage(Object object) {
        if (this.relayClient == null)
            return;

        var data = this.gson.toJson(object);
        this.relayClient.send(data);
    }

    /*
     * Listener methods.
     */

    @Override
    public void onStart() {
        this.logger.info("Packet visualizer started on port {}.", this.getPort());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (this.relayClient != null) {
            this.logger.warn("Another relay client has requested to connect.");
            this.logger.warn("The current client will be disconnected.");
        }

        this.relayClient = conn;
        this.logger.info("Relay client connected.");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.logger.info("Relay client disconnected.");
        this.relayClient = null;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        this.logger.warn("An error occurred when handling the relay client.", ex);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            var parsed = BreakingBedrock.getGson().fromJson(
                    message, PacketVisualizerMessage.class);

            switch (parsed.getPacketId()) {
                default -> throw new IllegalArgumentException("Unknown packet ID: " + parsed.getPacketId());
                case PacketIds.HANDSHAKE -> this.sendMessage(PacketVisualizerMessage.builder()
                        .packetId(PacketIds.HANDSHAKE).data(String.valueOf(System.currentTimeMillis())).build());
            }
        } catch (Exception exception) {
            this.logger.warn("An error occurred when handling a message from the relay client.", exception);
        }
    }
}
