package lol.magix.breakingbedrock.network.packets.java.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.game.containers.player.InventoryContainer;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

@Translate(PacketType.JAVA)
public final class UpdateSelectedSlotC2STranslator extends Translator<UpdateSelectedSlotC2SPacket> {
    @Override
    public Class<UpdateSelectedSlotC2SPacket> getPacketClass() {
        return UpdateSelectedSlotC2SPacket.class;
    }

    @Override
    public void translate(UpdateSelectedSlotC2SPacket packet) {
        var container = this.containers().getInventory();
        if (!(container instanceof InventoryContainer playerInventory)) return;

        playerInventory.updateHotbarItem(this.bedrockClient, packet.getSelectedSlot());
    }
}
