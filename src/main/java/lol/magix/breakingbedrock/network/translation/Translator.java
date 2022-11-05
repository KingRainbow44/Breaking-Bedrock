package lol.magix.breakingbedrock.network.translation;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import org.slf4j.Logger;

/**
 * Translates a Bedrock packet.
 * @param <P> The packet type.
 */
public abstract class Translator<P extends BedrockPacket> {
    protected final BedrockNetworkClient client = BedrockNetworkClient.getInstance();
    protected final Logger logger = this.client.getLogger();
    protected final boolean shouldLog = client.shouldLog();

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