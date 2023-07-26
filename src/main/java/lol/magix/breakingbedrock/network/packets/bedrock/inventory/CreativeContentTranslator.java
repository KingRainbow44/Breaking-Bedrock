package lol.magix.breakingbedrock.network.packets.bedrock.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.CreativeContentPacket;

import java.util.Arrays;

@Translate(PacketType.BEDROCK)
public final class CreativeContentTranslator extends Translator<CreativeContentPacket> {
    @Override
    public Class<CreativeContentPacket> getPacketClass() {
        return CreativeContentPacket.class;
    }

    @Override
    public void translate(CreativeContentPacket packet) {
        var creativeContainer = this.containers().getCreative();

        // Add the items.
        creativeContainer.getContents().addAll(
                Arrays.asList(packet.getContents()));
        // Update the mappings.
        creativeContainer.updateInventory();
    }
}
