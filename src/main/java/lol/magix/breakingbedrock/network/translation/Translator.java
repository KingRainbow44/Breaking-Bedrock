package lol.magix.breakingbedrock.network.translation;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.JavaNetworkClient;
import org.slf4j.Logger;

/**
 * Translates a Bedrock packet.
 * @param <P> The packet type.
 */
public abstract class Translator<P> {
    protected final BedrockNetworkClient bedrockClient = BedrockNetworkClient.getInstance();
    protected final Logger logger = this.bedrockClient.getLogger();
    protected final boolean shouldLog = bedrockClient.shouldLog();

    protected final JavaNetworkClient javaClient = this.bedrockClient.getJavaNetworkClient();

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