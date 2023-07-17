package lol.magix.breakingbedrock.translators.pack;

import lombok.Data;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePacksInfoPacket;

import java.io.File;
import java.util.UUID;

@Data
public final class ResourcePackInfo {
    /**
     * Creates a new ResourcePackInfo instance.
     *
     * @param entry The entry to create the ResourcePackInfo from.
     * @return The created ResourcePackInfo.
     */
    public static ResourcePackInfo from(ResourcePacksInfoPacket.Entry entry) {
        return new ResourcePackInfo(
                UUID.fromString(entry.getPackId()),
                entry.getPackVersion(),
                entry.getPackSize(),
                entry.getContentKey(),
                entry.getSubPackName(),
                entry.getContentId(),
                entry.isScripting(),
                entry.isRaytracingCapable()
        );
    }

    private final UUID packId;
    private final String packVersion;
    private final long packSize;
    private final String contentKey;
    private final String subPackName;
    private final String contentId;
    private final boolean scripting;
    private final boolean raytracingCapable;

    /**
     * @return The file of the pack.
     */
    public File getPackFile() {
        return new File(
                ResourcePackTranslator.CLIENT_CACHE,
                this.packId.toString());
    }
}
