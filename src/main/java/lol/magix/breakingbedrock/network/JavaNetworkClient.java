package lol.magix.breakingbedrock.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.client.util.telemetry.WorldSession;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles packet processing on the Java (local) client.
 */
public final class JavaNetworkClient {
    private final Logger logger
            = LoggerFactory.getLogger("Java Client");

    private final ClientConnection local;
    private final ClientPlayNetworkHandler localNetwork;

    public JavaNetworkClient() {
        var javaClient = MinecraftClient.getInstance();
        var bedrockClient = BedrockNetworkClient.getInstance();
        var authentication = bedrockClient.getAuthentication();

        var server = bedrockClient.getConnectionDetails();

        // Create local properties.
        this.local = new ClientConnection(NetworkSide.CLIENTBOUND);
        this.localNetwork = new ClientPlayNetworkHandler(
                javaClient, null, this.local,
                new ServerInfo("Bedrock Server", server.javaAddress(), false),
                new GameProfile(authentication.getIdentity(), authentication.getDisplayName()),
                new WorldSession(TelemetrySender.NOOP, false, null, null)
        );
    }

    /**
     * Translates a Java packet to Bedrock.
     * @param packet The Java packet.
     */
    public void processPacket(Packet<ClientPlayPacketListener> packet) {
        try {
            // Attempt to process the packet.
            packet.apply(this.localNetwork);
        } catch (OffThreadException ignored) {
            // Ignore.
        } catch (Exception exception) {
            this.logger.warn("Failed to process packet: " +
                    packet.getClass().getSimpleName(), exception);
        }
    }
}
