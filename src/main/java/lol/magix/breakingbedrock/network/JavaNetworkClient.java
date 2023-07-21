package lol.magix.breakingbedrock.network;

import com.mojang.authlib.GameProfile;
import lol.magix.breakingbedrock.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.client.util.telemetry.WorldSession;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import org.cloudburstmc.math.vector.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles packet processing on the Java (local) client.
 */
public final class JavaNetworkClient {
    private final Logger logger
            = LoggerFactory.getLogger("Java Client");
    @Getter private final List<ResourcePackProfile> resourcePacks
            = new ArrayList<>();

    private final BedrockNetworkClient handle;

    @Getter private final MinecraftClient client;
    @Getter private final ClientConnection local;
    @Getter private final ClientPlayNetworkHandler localNetwork;

    public JavaNetworkClient() {
        this.handle = BedrockNetworkClient.getInstance();

        var javaClient = MinecraftClient.getInstance();
        var authentication = this.handle.getAuthentication();

        var server = this.handle.getConnectionDetails();

        // Create local properties.
        this.local = new ClientConnection(NetworkSide.CLIENTBOUND);
        this.localNetwork = new ClientPlayNetworkHandler(
                javaClient, null, this.local,
                new ServerInfo("Bedrock Server", server.javaAddress(), false),
                new GameProfile(authentication.getIdentity(), authentication.getDisplayName()),
                new WorldSession(TelemetrySender.NOOP, false, null, null)
        );
        this.client = MinecraftClient.getInstance();
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

    /**
     * @return The player's position.
     */
    public Vector3f getPlayerPosition() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return Vector3f.ZERO;

        var pos = player.getPos();
        return Vector3f.from(pos.getX(), pos.getY() +
                player.getEyeHeight(EntityPose.STANDING), pos.getZ());
    }

    /**
     * Loads all resource packs from the server.
     */
    public void loadResourcePacks() {
        // Check if resource packs should be loaded.
        var activePacks = this.handle.getData().getActivePacks();
        if (activePacks.isEmpty()) return;

        this.resourcePacks.clear();

        // Load all server resource packs.
        for (var pack : activePacks) {
            var packFile = pack.getPackFile();
            if (!packFile.exists()) continue;

            this.loadResourcePack(packFile);
        }

        // Reload all resource packs.
        this.getClient().reloadResourcesConcurrently();
    }

    /**
     * Loads a resource pack.
     *
     * @param pack The resource pack to load.
     */
    public void loadResourcePack(File pack) {
        ResourcePackProfile.PackFactory packFactory = name ->
                new DirectoryResourcePack(name, pack.toPath(), false);
        var metadata = ResourcePackProfile.loadMetadata("server", packFactory);
        if (metadata == null) {
            this.logger.warn("Failed to load resource pack: {}.", pack.getName());
            return;
        }

        this.logger.info("Applying resource pack: {}.", pack.getName());
        var profile = ResourcePackProfile.of("server",
                Text.translatable("resourcePack.server.name"),
                true, packFactory, metadata,
                ResourceType.CLIENT_RESOURCES, InsertionPosition.TOP, true,
                ResourcePackSource.SERVER
        );

        this.resourcePacks.add(profile);
    }

    /**
     * Disconnects from the server.
     *
     * @param reason The reason for disconnecting.
     */
    public void disconnect(String reason) {
        var world = this.client.world;
        if (world != null) {
            // Disconnect from the world.
            world.disconnect();
        }

        // Process the disconnected packet.
        this.localNetwork.onDisconnect(new DisconnectS2CPacket(
                TextUtils.translate(reason)));
    }
}
