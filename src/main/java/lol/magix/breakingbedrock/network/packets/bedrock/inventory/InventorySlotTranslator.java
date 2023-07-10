package lol.magix.breakingbedrock.network.packets.bedrock.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.game.containers.player.InventoryContainer;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket;

@Translate(PacketType.BEDROCK)
public final class InventorySlotTranslator extends Translator<InventorySlotPacket> {
    @Override
    public Class<InventorySlotPacket> getPacketClass() {
        return InventorySlotPacket.class;
    }

    @Override
    public void translate(InventorySlotPacket packet) {
        var player = this.player();
        if (player == null) return;

        var containerId = packet.getContainerId();
        var slot = packet.getSlot();

        // Get the container.
        var container = this.containers().getContainer(containerId);
        if (container == null) {
            container = this.containers().getCurrentContainer();
        }

        // Update the slot in the container.
        container.setBedrockItem(slot, packet.getItem());
        this.run(() -> {
            // Update the hotbar item if it has changed.
            if (slot == player.getInventory().selectedSlot) {
                var inventory = this.containers().getInventory();
                if (inventory instanceof InventoryContainer playerInventory)
                    playerInventory.updateHotbarItem(this.bedrockClient, slot);
            }
        });
        this.run(container::updateInventory);
    }
}
