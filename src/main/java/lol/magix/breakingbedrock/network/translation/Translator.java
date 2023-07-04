package lol.magix.breakingbedrock.network.translation;

import com.google.gson.Gson;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.JavaNetworkClient;
import lol.magix.breakingbedrock.objects.game.SessionData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.slf4j.Logger;

/**
 * Translates a Bedrock packet.
 * @param <P> The packet type.
 */
public abstract class Translator<P> {
    protected final Gson gson = BreakingBedrock.getGson();

    protected final BedrockNetworkClient bedrockClient = BedrockNetworkClient.getInstance();
    protected final Logger logger = this.bedrockClient.getLogger();
    protected final boolean shouldLog = this.bedrockClient.shouldLog();

    /**
     * Returns the Java network client.
     * @return A {@link JavaNetworkClient} instance.
     */
    protected final JavaNetworkClient javaClient() {
        return this.bedrockClient.getJavaNetworkClient();
    }

    /**
     * Returns the Bedrock client's session flags.
     * @return A {@link SessionData} instance.
     */
    protected final SessionData data() {
        return this.bedrockClient.getData();
    }

    /**
     * @return The Minecraft client.
     */
    protected final MinecraftClient client() {
        return MinecraftClient.getInstance();
    }

    /**
     * @return The player.
     */
    protected final ClientPlayerEntity player() {
        return this.client().player;
    }

    /**
     * Runs the runnable on the main thread.
     * @param runnable The runnable.
     */
    protected final void run(Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
    }

    /**
     * Should return the packet's class.
     *
     * @return A {@link BedrockPacket} class.
     */
    public abstract Class<P> getPacketClass();

    /**
     * Translates the packet and handles it.
     * @param packet The Bedrock packet.
     */
    public abstract void translate(P packet);
}
