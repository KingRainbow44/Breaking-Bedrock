package lol.magix.breakingbedrock.network.packets.bedrock.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket;

@Translate(PacketType.BEDROCK)
public final class InventoryContentTranslator extends Translator<InventoryContentPacket> {
    @Override
    public Class<InventoryContentPacket> getPacketClass() {
        return InventoryContentPacket.class;
    }

    @Override
    public void translate(InventoryContentPacket packet) {
        // Get the container.
        var containerId = packet.getContainerId();
        var container = this.containers().getContainer(containerId);

        if (container == null) {
            container = this.containers().getCurrentContainer();
        }

        // Write the container contents.
        var contents = packet.getContents();
        for (var i = 0; i < contents.size(); i++) {
            container.setBedrockItem(i, contents.get(i));
        }
        this.run(container::updateInventory);
    }
}
