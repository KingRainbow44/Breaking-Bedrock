package lol.magix.breakingbedrock.network.packets.bedrock.packs;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.pack.ResourcePackInfo;
import lol.magix.breakingbedrock.translators.pack.ResourcePackTranslator;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import org.cloudburstmc.protocol.bedrock.packet.ClientCacheStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket.Status;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePacksInfoPacket;

import java.util.ArrayList;
import java.util.UUID;

@Translate(PacketType.BEDROCK)
public final class ResourcePackInfoTranslator extends Translator<ResourcePacksInfoPacket> {
    @Override
    public Class<ResourcePacksInfoPacket> getPacketClass() {
        return ResourcePacksInfoPacket.class;
    }

    @Override
    public void translate(ResourcePacksInfoPacket packet) {
        this.bedrockClient.sendPacket(new ClientCacheStatusPacket(), true);

        var packCache = ResourcePackTranslator.getCache();

        // Get all resource packs which need to be downloaded.
        var packs = new ArrayList<String>();
        packet.getResourcePackInfos().forEach(pack -> {
            // Check if pack is already downloaded.
            var packId = UUID.fromString(pack.getPackId());
            if (!ResourcePackTranslator.packDownloaded(packId))
                packs.add(pack.getPackId() + "_" + pack.getPackVersion());

            // Add the resource pack to the cache.
            if (!packCache.containsKey(pack.getPackId()))
                packCache.put(pack.getPackId(), ResourcePackInfo.from(pack));

            // Set the resource packs for the client.
            this.data().getActivePacks()
                    .add(packCache.get(pack.getPackId()));
        });

        // Create resource pack response.
        var response = new ResourcePackClientResponsePacket();
        response.setStatus(packs.isEmpty() ?
                Status.HAVE_ALL_PACKS : Status.SEND_PACKS);

        // Add resource pack IDs if there are any.
        if (!packs.isEmpty()) {
            response.getPackIds().addAll(packs);
            this.data().setPacksDownloaded(false);
        }

        // Send resource pack response.
        this.bedrockClient.sendPacket(response, true);
    }
}
