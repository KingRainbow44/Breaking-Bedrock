package lol.magix.breakingbedrock.network;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import io.netty.buffer.ByteBuf;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Handles batch packets.
 */
public final class NetworkBatchHandler implements BatchHandler {
    private final Logger logger = BedrockNetworkClient.getInstance().getLogger();

    @Override public void handle(BedrockSession session, ByteBuf compressed, Collection<BedrockPacket> packets) {
        for (var packet : packets) {
            // TODO: Translate packets.

            if (session.isLogging()) {
                var packetDump = EncodingUtils.encodePacket(packet);
                this.logger.info("[<-] {}: {}", session.getAddress(), packetDump);
            }
        }
    }
}