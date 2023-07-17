package lol.magix.breakingbedrock.objects.absolute;

import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.util.Identifier;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public interface GameConstants {
    /* The player entity offset. */
    float PLAYER_OFFSET = 1.62F;

    /* The current Java resource pack format. */
    int RP_FORMAT = 15;

    /* The block definitions. */
    AtomicReference<SimpleDefinitionRegistry<BlockDefinition>> BLOCKS = new AtomicReference<>();
    /* The registry manager for login. */
    AtomicReference<DynamicRegistryManager.Immutable> REGISTRY = new AtomicReference<>();

    /* The registry NBT definition. */
    RegistryOps<NbtElement> REGISTRY_OPS = RegistryOps.of(
            NbtOps.INSTANCE, DynamicRegistryManager.of(Registries.REGISTRIES));

    /* The fallback block for invalid block states. */
    Block FALLBACK_BLOCK = Registries.BLOCK.get(new Identifier(
            System.getProperty("FallbackBlock", "minecraft:stone")));

    /* The default server address. */
    String DEFAULT_SERVER = System.getProperty("DefaultServerAddress", "127.0.0.1");
    /* The default server port. */
    String DEFAULT_PORT = System.getProperty("DefaultServerPort", "19132");
    /* The authentication enabled default. */
    boolean DEFAULT_AUTHENTICATION = Boolean.parseBoolean(System.getProperty("DefaultAuthentication", "true"));

    /**
     * Loads the registry from file.
     */
    static void loadRegistry() {
        try (var registry = ResourceUtils.getResourceAsStream(Resources.REGISTRY)) {
            var bytes = registry.readAllBytes();
            var buffer = PacketByteBufs.create();

            // Load the bytes into the buffer.
            buffer.writeBytes(bytes);
            // Read the bytes as a registry.
            REGISTRY.set(buffer.decode(REGISTRY_OPS,
                    SerializableRegistries.CODEC).toImmutable());
        } catch (IOException ignored) {
            BreakingBedrock.getLogger().warn("Unable to load registry.");
        }
    }
}
