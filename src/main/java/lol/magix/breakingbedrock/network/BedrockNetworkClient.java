package lol.magix.breakingbedrock.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Promise;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.events.EventManager;
import lol.magix.breakingbedrock.network.auth.Authentication;
import lol.magix.breakingbedrock.objects.ConnectionDetails;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.objects.absolute.PacketVisualizer;
import lol.magix.breakingbedrock.objects.definitions.visualizer.PacketVisualizerData;
import lol.magix.breakingbedrock.objects.game.AuthInputHandler;
import lol.magix.breakingbedrock.objects.game.SessionData;
import lol.magix.breakingbedrock.objects.game.caches.BlockEntityDataCache;
import lol.magix.breakingbedrock.utils.IntervalUtils;
import lol.magix.breakingbedrock.utils.ProfileUtils;
import lol.magix.breakingbedrock.utils.ScreenUtils;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.data.PlayerBlockActionData;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockClientInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestNetworkSettingsPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handles network connections to a Bedrock server.
 */
public final class BedrockNetworkClient {
    @Getter private static final BedrockNetworkClient instance = new BedrockNetworkClient();

    @Getter private final Logger logger = LoggerFactory.getLogger("Bedrock Client");
    @Getter private final EventManager eventManager = new EventManager();

    /**
     * Returns the {@link BedrockClientSession} handle.
     * @return A {@link BedrockClientSession} instance.
     */
    public static BedrockClientSession getHandle() {
        return BedrockNetworkClient.getInstance().session;
    }

    private boolean hasLoggedIn = false;

    @Getter private BedrockClientSession session = null;
    @Getter private SessionData data = null;
    @Getter private Authentication authentication = null;
    @Getter private ConnectionDetails connectionDetails = null;

    @Getter private JavaNetworkClient javaNetworkClient = null;

    @Getter private AuthInputHandler inputHandler = null;
    @Getter private BlockEntityDataCache blockEntityDataCache = null;
    @Getter private List<PlayerBlockActionData> blockActions = null;

    /**
     * Initializes a connection with a server.
     */
    public void connect(ConnectionDetails connectTo) {
        this.connectionDetails = connectTo;

        // Update screen.
        // ScreenUtils.connect();

        // Connect to the server.
        this.connect().addListener((Promise<BedrockClientSession> promise) -> {
            if (!promise.isSuccess()) {
                var throwable = promise.cause();
                this.getLogger().warn("Unable to connect to server.", throwable);
                MinecraftClient.getInstance().execute(() ->
                        ScreenUtils.disconnect(Text.of(throwable.getMessage())));
            } else {
                this.session = promise.getNow();
                this.onSessionInitialized();
            }
        });
    }

    /**
     * Attempts to log in to the server.
     */
    private Promise<BedrockClientSession> connect() {
        // Fetch a backend event loop.
        var loop = BreakingBedrock.getEventGroup().next();
        Promise<BedrockClientSession> promise = loop.newPromise();

        // Create a new bootstrap.
        new Bootstrap()
                .group(loop)
                .option(RakChannelOption.RAK_MTU, 1400)
                .option(RakChannelOption.RAK_ORDERING_CHANNELS, 1)
                .option(RakChannelOption.RAK_SESSION_TIMEOUT, 10000L)
                .option(RakChannelOption.RAK_CONNECT_TIMEOUT, 25 * 1000L)
                .option(RakChannelOption.RAK_PROTOCOL_VERSION,
                        NetworkConstants.PACKET_CODEC.getRaknetProtocolVersion())
                .channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
                .handler(new BedrockClientInitializer() {
                    @Override
                    protected void initSession(BedrockClientSession session) {
                        var instance = BedrockNetworkClient.this;
                        instance.getLogger().info("RakNet session initialized.");

                        // Set session properties.
                        session.setCodec(NetworkConstants.PACKET_CODEC);
                        session.setPacketHandler(new BedrockPacketHandler(instance));

                        // Fulfill the promise.
                        promise.trySuccess(session);
                    }
                })
                .connect(this.connectionDetails.toSocketAddress())
                .addListener((ChannelFuture future) -> {
                    if (!future.isSuccess()) {
                        promise.tryFailure(future.cause());
                        future.channel().close();
                    }
                });

        return promise;
    }

    /**
     * Invoked when the client has successfully connected to the server.
     */
    private void onSessionInitialized() {
        // Set session properties.
        this.session.setLogging(BreakingBedrock.isDebugEnabled());

        // Create a session flags instance.
        this.data = new SessionData();

        try {
            // Request protocol version from server.
            var requestPacket = new RequestNetworkSettingsPacket();
            requestPacket.setProtocolVersion(this.session.getCodec().getProtocolVersion());
            this.sendPacket(requestPacket, true);

            // Apply compression properties.
            // this.client.setCompression(PacketCompressionAlgorithm.ZLIB);
            // this.client.setCompressionLevel(-1);

            // Wait 2s, then send login packet.
            IntervalUtils.runAfter(this::loginToServer, 2000);
        } catch (Exception exception) {
            this.logger.error("An error occurred while logging in.", exception);
            this.session.close("Login error");
        }
    }

    /*
     * Internal utility methods.
     */

    /**
     * @param data The input data.
     */
    public void addInputData(PlayerAuthInputData data) {
        this.getInputHandler().getInputData().add(data);
    }

    /**
     * Invoked when the client is disconnected.
     * @param reason The reason for disconnection.
     */
    public void onDisconnect(String reason) {
        // Display a client disconnect screen.
        if (this.data.isInitialized())
            MinecraftClient.getInstance().execute(() ->
                    ScreenUtils.disconnect(Text.of(reason)));

        // Invalidate client properties.
        this.hasLoggedIn = false;
        this.data = null;
        this.session = null;
        this.authentication = null;
        this.connectionDetails = null;
        this.javaNetworkClient = null;
    }

    /**
     * Attempts to send a {@link LoginPacket} to the server.
     * This will start a client login if successful.
     */
    public void loginToServer() {
        // Validate that we haven't already logged in.
        if (this.hasLoggedIn) return;
        this.hasLoggedIn = true;

        try {
            // Attempt to log into server.
            var loginPacket = new LoginPacket();

            // Attempt to authenticate.
            this.authentication = new Authentication();
            var chainData = this.connectionDetails.online() ?
                    this.authentication.getOnlineChainData() :
                    this.authentication.getOfflineChainData(BreakingBedrock.getUsername());
            // Pull profile data.
            var profile = ProfileUtils.getProfileData(this);
            if (profile == null) profile = ProfileUtils.SKIN_DATA_BASE_64;

            // Set session data.
            this.data.setDisplayName(this.authentication.getDisplayName());
            this.data.setIdentity(this.authentication.getIdentity());
            this.data.setXuid(this.authentication.getXuid());

            // Set the login properties.
            loginPacket.setProtocolVersion(this.session.getCodec().getProtocolVersion());
            loginPacket.getChain().addAll(chainData);
            loginPacket.setExtra(profile);

            // Send the packet & update connection.
            this.sendPacket(loginPacket, true);
            this.javaNetworkClient = new JavaNetworkClient();
        } catch (Exception exception) {
            this.logger.error("An error occurred while logging in.", exception);
            this.session.close("Login error");
        }
    }

    /**
     * Checks if the client session supports logging.
     * @return True if logging should be performed.
     */
    public boolean shouldLog() {
        return BreakingBedrock.isDebugEnabled() || (
                this.session != null && this.session.isLogging()
        );
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
                this.session != null && // Check if the client has been initialized.
                this.session.isConnected() && // Check if the client is connected.
                this.session.getPeer().isConnected(); // Check if the peer is connected.
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
        if (immediate)
            this.session.sendPacketImmediately(packet);
        else
            this.session.sendPacket(packet);

        // Log packet if needed.
        if (this.session.isLogging()) {
            // Visualize outbound packet.
            PacketVisualizer.getInstance().sendMessage(
                    PacketVisualizerData.toMessage(packet, true));
        }
    }

    /*
     * Event methods.
     */

    /**
     * Invoked when the Java player is ready to play.
     */
    public void onPlayerInitialization() {
        this.blockActions = new LinkedList<>();
        // TODO: Initialize container manager.
        this.blockEntityDataCache = new BlockEntityDataCache();
        // TODO: Set the open container.
        this.inputHandler = new AuthInputHandler(this);
    }
}
