package lol.magix.breakingbedrock.translators.pack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lombok.Data;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackChunkRequestPacket;

import java.io.File;
import java.nio.file.Files;

@Data
public final class ResourcePackDownloadHandle {
    private final BedrockNetworkClient client;
    private final ResourcePackInfo packInfo;

    private final long chunks;

    private int currentChunk = 0;
    private final ByteBuf packData = Unpooled.buffer();

    /**
     * Requests the next chunk of the resource pack.
     */
    public void requestChunk() {
        var packInfo = this.getPackInfo();

        // Create chunk request packet.
        var chunkPacket = new ResourcePackChunkRequestPacket();
        chunkPacket.setPackId(packInfo.getPackId());
        chunkPacket.setChunkIndex(this.currentChunk++);
        chunkPacket.setPackVersion(packInfo.getPackVersion());

        // Send chunk request packet.
        this.client.sendPacket(chunkPacket, true);
        BreakingBedrock.getLogger().debug("Requested chunk {} of {} for resource pack {}.",
                this.getCurrentChunk(), this.getChunks(), packInfo.getPackId());
    }

    /**
     * @return True if there are more chunks to request.
     */
    public boolean hasMore() {
        return this.getCurrentChunk() < this.getChunks() ||
                (this.getChunks() == 0 &&
                        this.getPackData().writerIndex() <
                                this.getPackInfo().getPackSize());
    }

    /**
     * Invoked when all resource pack data is received.
     */
    public void completeDownload() {
        try {
            // Save the resource pack to the file system.
            var file = new File(ResourcePackTranslator.SERVER_CACHE,
                    this.getPackInfo().getPackId() + ".zip");
            // Check if the file exists.
            if (file.exists()) {
                // Delete the file.
                if (!file.delete()) {
                    BreakingBedrock.getLogger().warn("Unable to delete existing resource pack.");
                    return;
                }
            }
            // Write the resource pack to the file system.
            Files.write(file.toPath(), this.getPackData().array());

            BreakingBedrock.getLogger().debug("Finished downloading resource pack {}. (total chunks: {})",
                    this.getPackInfo().getPackId(), this.getCurrentChunk());
        } catch (Exception exception) {
            BreakingBedrock.getLogger().warn("Unable to save resource pack.", exception);
        } finally {
            this.getPackData().release();
        }
    }
}
