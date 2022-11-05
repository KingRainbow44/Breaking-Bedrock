package lol.magix.breakingbedrock.network;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockClientSession;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.v557.Bedrock_v557;
import io.netty.util.AsciiString;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.network.auth.Authentication;
import lol.magix.breakingbedrock.objects.ConnectionDetails;
import lol.magix.breakingbedrock.objects.PacketVisualizer;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerData;
import lol.magix.breakingbedrock.utils.NetworkUtils;
import lol.magix.breakingbedrock.utils.ScreenUtils;
import lol.magix.breakingbedrock.utils.SkinUtils;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Handles network connections to a Bedrock server.
 */
public final class BedrockNetworkClient {
    @Getter private static BedrockNetworkClient instance = new BedrockNetworkClient();
    @Getter private final Logger logger = LoggerFactory.getLogger("Bedrock Client");

    /**
     * Returns the {@link BedrockClient} handle.
     * @return A {@link BedrockClient} instance.
     */
    public static BedrockClient getHandle() {
        return BedrockNetworkClient.getInstance().client;
    }

    /**
     * Returns the {@link BedrockClientSession} for the client handle.
     * @return A {@link BedrockClientSession} instance.
     */
    public static BedrockClientSession getSession() {
        return BedrockNetworkClient.getHandle().getSession();
    }

    private BedrockClient client = null;
    @Getter private Authentication authentication = null;
    @Getter private ConnectionDetails connectionDetails = null;

    private JavaNetworkClient javaNetworkClient = null;

    /**
     * Initializes a connection with a server.
     */
    public void connect(ConnectionDetails connectTo) {
        this.connectionDetails = connectTo;

        // Create a client & bind to a port.
        this.bindToClient();
        // Update client properties.
        this.client.setRakNetVersion(NetworkConstants.PACKET_CODEC.getRaknetProtocolVersion());
        // Connect to the server.
        this.client.connect(connectTo.toSocketAddress()).whenComplete(this::onConnect);
    }

    /**
     * Invoked when the client connects to the server.
     */
    private void onConnect(BedrockSession session, Throwable throwable) {
        // Check if there was a connection error.
        if (throwable != null) {
            MinecraftClient.getInstance().execute(() ->
                    ScreenUtils.disconnect(Text.of(throwable.getMessage())));
            return;
        }

        // Create a new session handler.
        this.onSessionInitialized(session);
    }

    /**
     * Invoked when the client is disconnected.
     * @param reason The reason for disconnection.
     */
    private void onDisconnect(DisconnectReason reason) {
        MinecraftClient.getInstance().execute(() ->
                ScreenUtils.disconnect(NetworkUtils.getDisconnectReason(reason)));
    }

    /**
     * Invoked when the client has successfully connected to the server.
     */
    private void onSessionInitialized(BedrockSession session) {
        // Set session properties.
        session.setLogging(BreakingBedrock.isDebugEnabled());
        session.setPacketCodec(NetworkConstants.PACKET_CODEC);
        // Register session handlers.
        session.setBatchHandler(new NetworkBatchHandler());
        session.addDisconnectHandler(this::onDisconnect);

        try {
            // Attempt to log into server.
            var loginPacket = new LoginPacket();

            // Attempt to authenticate.
            this.authentication = new Authentication();
            var chainData = this.connectionDetails.online() ?
                    this.authentication.getOnlineChainData() :
                    this.authentication.getOfflineChainData(BreakingBedrock.getUsername());
            // Pull skin data.
            var skinData = SkinUtils.getSkinData(this);
            if (skinData == null) skinData = SkinUtils.SKIN_DATA_BASE_64;

            // Set the login properties.
            loginPacket.setProtocolVersion(session.getPacketCodec().getProtocolVersion());
            loginPacket.setChainData(new AsciiString(chainData));
            loginPacket.setSkinData(new AsciiString(skinData));

            // Send the packet & update connection.
            this.sendPacket(loginPacket, true);
            this.javaNetworkClient = new JavaNetworkClient();
        } catch (Exception exception) {
            this.logger.error("An error occurred while logging in.", exception);
            this.client.close();
        }
    }

    /*
     * Internal utility methods.
     */

    /**
     * Attempts to bind to a port.
     */
    private void bindToClient() {
        var success = false;
        while (!success) try {
            var bindAddress = new InetSocketAddress("0.0.0.0", NetworkUtils.getOpenPort());
            this.client = new BedrockClient(bindAddress);
            this.client.bind().join();
            success = true;
        } catch (Exception ignored) { }
    }

    /**
     * Checks if the client session supports logging.
     * @return True if logging should be performed.
     */
    public boolean shouldLog() {
        return this.client != null && this.client.getSession() != null && this.client.getSession().isLogging();
    }

    /*
     * General networking methods.
     */

    /**
     * Checks if the client is connected to a server.
     * @return True if connected, false otherwise.
     */
    public boolean isConnected() {
        return
                this.client != null && // Check if the client has been initialized.
                this.client.getSession() != null && // Check if the session has been initialized.
                this.client.getRakNet() != null && // Check if the RakNet client has been initialized.
                this.client.getRakNet().isRunning(); // Check if the RakNet client is running.
    }

    /**
     * Sends a packet to the client.
     * This does not happen immediately.
     * @param packet The packet to send.
     */
    public void sendPacket(BedrockPacket packet) {
        this.sendPacket(packet, false);
    }

    /**
     * Sends a packet to the client.
     * @param packet The packet to send.
     * @param immediate Whether to send the packet immediately.
     */
    public void sendPacket(BedrockPacket packet, boolean immediate) {
        // Set the packet's ID.
        packet.setPacketId(NetworkConstants.PACKET_CODEC
                .getId(packet.getClass()));

        var session = this.client.getSession();
        if (immediate)
            session.sendPacketImmediately(packet);
        else
            session.sendPacket(packet);

        // Log packet if needed.
        if (session.isLogging()) {
            // Visualize outbound packet.
            PacketVisualizer.getInstance().sendMessage(
                    PacketVisualizerData.toMessage(packet, true));
        }
    }
}
