package lol.magix.breakingbedrock.network.packets.java.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

@Translate(PacketType.JAVA)
public final class PlayerInteractItemC2STranslator extends Translator<PlayerInteractItemC2SPacket> {
    @Override
    public Class<PlayerInteractItemC2SPacket> getPacketClass() {
        return PlayerInteractItemC2SPacket.class;
    }

    @Override
    public void translate(PlayerInteractItemC2SPacket packet) {
        var player = this.player();
        var world = this.client().world;
        if (player == null || world == null) return;

        var containers = this.containers();
        var selectedSlot = player.getInventory().selectedSlot;

        var itemStack = packet.getHand() == Hand.MAIN_HAND ?
                containers.getInventory().getItem(selectedSlot) :
                containers.getOffhand().getItem(0);

        // Get the block the player is looking at.
        var result = GameUtils.getLookingBlock(player, world);
        if (result == null) return;

        var blockPos = result.a();
        var block = result.b();
        var sourcePos = result.c();

        // Create an inventory transaction.
        var transactionPacket = new InventoryTransactionPacket();
        transactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
        transactionPacket.setActionType(1);
        transactionPacket.setBlockPosition(GameUtils.toBlockPos(blockPos));
        transactionPacket.setHotbarSlot(selectedSlot);
        transactionPacket.setItemInHand(itemStack);
        transactionPacket.setPlayerPosition(this.javaClient().getPlayerPosition());
        transactionPacket.setClickPosition(GameUtils.convert(sourcePos.subtract(
                blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        transactionPacket.setBlockDefinition(BlockStateTranslator.translate(block));
        this.bedrockClient.sendPacket(transactionPacket);
    }
}
