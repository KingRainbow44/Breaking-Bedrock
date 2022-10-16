package lol.magix.breakingbedrock.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;

/**
 * Handles packet processing on the Java (local) client.
 */
public final class JavaNetworkClient {
    private final ClientConnection local;
    private final ClientPlayNetworkHandler localNetwork;

    public JavaNetworkClient() {
        var javaClient = MinecraftClient.getInstance();
        var bedrockClient = BedrockNetworkClient.getInstance();
        var authentication = bedrockClient.getAuthentication();

        // Create local properties.
        this.local = new ClientConnection(NetworkSide.CLIENTBOUND);
        this.localNetwork = new ClientPlayNetworkHandler(javaClient, null, this.local,
                new GameProfile(authentication.getIdentity(), authentication.getDisplayName()),
                javaClient.createTelemetrySender());
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
        }
    }
}
