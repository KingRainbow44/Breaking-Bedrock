package lol.magix.breakingbedrock.game.containers.player;

import lol.magix.breakingbedrock.game.containers.GenericContainer;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.client.MinecraftClient;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket;

public final class InventoryContainer extends GenericContainer {
    /**
     * Creates an empty container with 36 slots.
     */
    public InventoryContainer() {
        super(36);
    }

    @Override
    public int getJavaSlotId(int bedrockSlotId) {
        return bedrockSlotId < 9 ? bedrockSlotId + 36 :
                super.getJavaSlotId(bedrockSlotId);
    }

    @Override
    public int getBedrockSlotId(int javaSlotId) {
        return javaSlotId >= 36 && javaSlotId < 45 ?
                javaSlotId - 36 : super.getBedrockSlotId(javaSlotId);
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void updateInventory() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Cannot update inventory with no player.");

        var screenHandler = player.playerScreenHandler;
        for (var i = 0; i < this.getSize(); i++)
            screenHandler.getSlot(this.getJavaSlotId(i))
                    .setStack(ItemTranslator.bedrock2Java(this.getItem(i)));
    }

    /**
     * Updates the hotbar item.
     *
     * @param client The client.
     * @param slot The slot.
     */
    public void updateHotbarItem(BedrockNetworkClient client, int slot) {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("Slot must be between 0 and 8.");

        var equipmentPacket = new MobEquipmentPacket();
        equipmentPacket.setRuntimeEntityId(client.getData().getRuntimeId());
        equipmentPacket.setItem(this.getItem(slot));
        equipmentPacket.setInventorySlot(slot);
        equipmentPacket.setHotbarSlot(slot);
        equipmentPacket.setContainerId(ContainerId.INVENTORY);

        client.sendPacket(equipmentPacket);
    }
}
